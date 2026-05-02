package com.capgemini.training.middleware.adapter.out.feign;
import feign.RequestInterceptor; import feign.RequestTemplate;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import java.util.UUID;
@Component
public class CorrelationIdInterceptor implements RequestInterceptor {
    @Override
    public void apply(RequestTemplate t) {
        String corrId = MDC.get("correlationId");
        if (corrId == null || corrId.isBlank()) { corrId = UUID.randomUUID().toString(); MDC.put("correlationId", corrId); }
        t.header("X-Correlation-ID", corrId);
    }
}
