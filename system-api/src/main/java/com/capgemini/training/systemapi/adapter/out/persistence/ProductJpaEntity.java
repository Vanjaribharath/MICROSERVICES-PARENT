package com.capgemini.training.systemapi.adapter.out.persistence;

import com.capgemini.training.systemapi.domain.model.ProductStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Day 6 — JPA entity. LAZY fetch everywhere to prevent N+1.
 * separate from domain Product record.
 */
@Entity
@Table(name = "products", indexes = {
    @Index(name = "idx_products_status",   columnList = "status"),
    @Index(name = "idx_products_category", columnList = "category_id"),
    @Index(name = "idx_products_name",     columnList = "name")
})
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor @Builder
public class ProductJpaEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 200) private String name;
    @Column(nullable = false, precision = 12, scale = 2) private BigDecimal price;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private ProductStatus status;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private CategoryJpaEntity category;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    private SupplierJpaEntity supplier;
    @CreatedDate @Column(updatable = false) private LocalDateTime createdAt;
    @LastModifiedDate private LocalDateTime updatedAt;
}
