package com.capgemini.training.systemapi.adapter.in.web;

import com.capgemini.training.systemapi.adapter.out.persistence.SupplierJpaEntity;
import com.capgemini.training.systemapi.adapter.out.persistence.SupplierJpaRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;

/** Day 8 — Supplier CRUD controller. */
@RestController
@RequestMapping("/api/v1/suppliers")
@RequiredArgsConstructor
@Tag(name = "Suppliers")
public class SupplierController {
    private final SupplierJpaRepository repo;

    @GetMapping public List<SupplierJpaEntity> list() { return repo.findAll(); }

    @GetMapping("/{id}")
    public ResponseEntity<SupplierJpaEntity> getById(@PathVariable Long id) {
        return repo.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<SupplierJpaEntity> create(@RequestBody SupplierRequest req) {
        SupplierJpaEntity saved = repo.save(SupplierJpaEntity.builder()
            .name(req.name()).contactEmail(req.contactEmail()).build());
        return ResponseEntity.created(URI.create("/api/v1/suppliers/" + saved.getId())).body(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!repo.existsById(id)) return ResponseEntity.notFound().build();
        repo.deleteById(id); return ResponseEntity.noContent().build();
    }

    public record SupplierRequest(@NotBlank String name, String contactEmail) {}
}
