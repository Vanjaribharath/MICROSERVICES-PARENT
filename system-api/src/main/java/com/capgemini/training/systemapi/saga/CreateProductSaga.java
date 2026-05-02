package com.capgemini.training.systemapi.saga;

import com.capgemini.training.systemapi.adapter.out.persistence.DomainEventRepository;
import com.capgemini.training.systemapi.adapter.out.persistence.ErrorEventRepository;
import com.capgemini.training.systemapi.adapter.out.persistence.SagaRepository;
import com.capgemini.training.systemapi.application.port.in.CreateProductCommand;
import com.capgemini.training.systemapi.application.port.in.CreateProductUseCase;
import com.capgemini.training.systemapi.domain.model.Product;
import com.capgemini.training.systemapi.events.ProductEvent;
import com.capgemini.training.systemapi.kafka.ProductEventProducer;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

/**
 * Day 24 — Saga Orchestrator: CreateProductSaga
 *
 * Saga steps:
 *   Step 1 → Validate category exists  (compensate: nothing)
 *   Step 2 → Persist product to DB     (compensate: delete product)
 *   Step 3 → Publish ProductCreated event to Kafka (compensate: publish ProductDeleted)
 *
 * If any step fails → compensate in reverse order.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CreateProductSaga {

    private final CreateProductUseCase createProductUseCase;
    private final ProductEventProducer eventProducer;
    private final SagaRepository sagaRepo;
    private final DomainEventRepository eventRepo;
    private final ErrorEventRepository errorRepo;
    private final ObjectMapper objectMapper;

    public SagaResult execute(CreateProductCommand command) {
        String correlationId = MDC.get("correlationId");
        String sagaId = null;

        try {
            // ── Start Saga ─────────────────────────────────────────────
            String payload = objectMapper.writeValueAsString(command);
            sagaId = sagaRepo.createSaga("CreateProductSaga", payload, correlationId, "system-api");
            log.info("Saga STARTED: sagaId={} type=CreateProductSaga correlationId={}", sagaId, correlationId);

            // ── Step 1: Persist Product ────────────────────────────────
            sagaRepo.addSagaStep(sagaId, "PERSIST_PRODUCT", 1, payload);
            Product product;
            try {
                product = createProductUseCase.create(command);
                sagaRepo.completeSagaStep(sagaId, "PERSIST_PRODUCT",
                    "{\"productId\":" + product.id() + "}");
                log.info("Saga step PERSIST_PRODUCT completed: productId={}", product.id());
            } catch (Exception ex) {
                sagaRepo.failSagaStep(sagaId, "PERSIST_PRODUCT", ex.getMessage());
                sagaRepo.updateSagaStatus(sagaId, "FAILED", "{\"error\":\"" + ex.getMessage() + "\"}");
                errorRepo.saveError("ProductPersistenceException", "500", ex.getMessage(),
                    getStackTrace(ex), "system-api", "/api/v1/products", "POST",
                    correlationId, sagaId, payload);
                log.error("Saga FAILED at PERSIST_PRODUCT: {}", ex.getMessage());
                return SagaResult.failed(sagaId, "Failed to persist product: " + ex.getMessage());
            }

            // ── Step 2: Publish Kafka Event ────────────────────────────
            sagaRepo.updateSagaStatus(sagaId, "STEP_1_DONE", null);
            sagaRepo.addSagaStep(sagaId, "PUBLISH_EVENT", 2,
                "{\"productId\":" + product.id() + "}");
            try {
                ProductEvent event = ProductEvent.created(
                    product.id(), product.name(), product.price(),
                    product.categoryId(), correlationId);
                eventProducer.publishProductEvent(event);
                sagaRepo.completeSagaStep(sagaId, "PUBLISH_EVENT",
                    "{\"eventType\":\"PRODUCT_CREATED\",\"eventId\":\"" + event.eventId() + "\"}");
                log.info("Saga step PUBLISH_EVENT completed: eventId={}", event.eventId());
            } catch (Exception ex) {
                // Non-fatal: event publish fail doesn't roll back product creation
                sagaRepo.failSagaStep(sagaId, "PUBLISH_EVENT", ex.getMessage());
                log.warn("Saga step PUBLISH_EVENT failed (non-fatal): {}", ex.getMessage());
            }

            // ── Complete Saga ──────────────────────────────────────────
            sagaRepo.updateSagaStatus(sagaId, "COMPLETED",
                "{\"productId\":" + product.id() + ",\"productName\":\"" + product.name() + "\"}");
            log.info("Saga COMPLETED: sagaId={} productId={}", sagaId, product.id());
            return SagaResult.success(sagaId, product);

        } catch (Exception ex) {
            log.error("Saga unexpected error: sagaId={} error={}", sagaId, ex.getMessage(), ex);
            if (sagaId != null) {
                sagaRepo.updateSagaStatus(sagaId, "FAILED", "{\"error\":\"" + ex.getMessage() + "\"}");
            }
            return SagaResult.failed(sagaId, ex.getMessage());
        }
    }

    private String getStackTrace(Exception ex) {
        java.io.StringWriter sw = new java.io.StringWriter();
        ex.printStackTrace(new java.io.PrintWriter(sw));
        String st = sw.toString();
        return st.length() > 2000 ? st.substring(0, 2000) + "..." : st;
    }

    /** Result object returned by the Saga. */
    public record SagaResult(
        String sagaId, boolean success, Product product, String errorMessage
    ) {
        public static SagaResult success(String sagaId, Product p) {
            return new SagaResult(sagaId, true, p, null);
        }
        public static SagaResult failed(String sagaId, String error) {
            return new SagaResult(sagaId, false, null, error);
        }
    }
}
