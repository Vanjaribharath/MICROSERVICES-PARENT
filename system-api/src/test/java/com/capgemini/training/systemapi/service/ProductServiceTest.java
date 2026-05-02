package com.capgemini.training.systemapi.service;

import com.capgemini.training.systemapi.application.port.in.CreateProductCommand;
import com.capgemini.training.systemapi.application.port.out.LoadProductPort;
import com.capgemini.training.systemapi.application.port.out.SaveProductPort;
import com.capgemini.training.systemapi.application.service.ProductService;
import com.capgemini.training.systemapi.domain.model.Product;
import com.capgemini.training.systemapi.domain.model.ProductStatus;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** Day 10 — Unit tests with Mockito. No Spring context needed. */
@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock SaveProductPort savePort;
    @Mock LoadProductPort loadPort;
    @InjectMocks ProductService service;

    @Test
    void create_shouldSaveAndReturnProduct() {
        var cmd = new CreateProductCommand("Widget", new BigDecimal("29.99"), 1L, null);
        var expected = new Product(1L, "Widget", new BigDecimal("29.99"),
            ProductStatus.ACTIVE, 1L, null, LocalDateTime.now(), LocalDateTime.now());
        when(savePort.save(any())).thenReturn(expected);

        Product result = service.create(cmd);

        assertThat(result.name()).isEqualTo("Widget");
        assertThat(result.status()).isEqualTo(ProductStatus.ACTIVE);
        verify(savePort).save(any());
    }

    @Test
    void create_shouldTrimName() {
        var cmd = new CreateProductCommand("  Widget  ", new BigDecimal("9.99"), 1L, null);
        var saved = new Product(1L, "Widget", new BigDecimal("9.99"),
            ProductStatus.ACTIVE, 1L, null, LocalDateTime.now(), LocalDateTime.now());
        when(savePort.save(any())).thenReturn(saved);
        assertThat(service.create(cmd).name()).isEqualTo("Widget");
    }

    @Test
    void domain_shouldRejectNegativePrice() {
        assertThatThrownBy(() ->
            new Product(null, "Bad", new BigDecimal("-1.00"),
                ProductStatus.ACTIVE, 1L, null, null, null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("negative");
    }

    @Test
    void getById_shouldThrowWhenNotFound() {
        when(loadPort.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getById(99L))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining("99");
    }

    @Test
    void delete_shouldThrowWhenNotFound() {
        when(savePort.existsById(999L)).thenReturn(false);
        assertThatThrownBy(() -> service.delete(999L))
            .isInstanceOf(EntityNotFoundException.class);
    }
}
