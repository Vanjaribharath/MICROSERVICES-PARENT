package com.capgemini.training.systemapi.adapter.in.web;

import com.capgemini.training.systemapi.adapter.out.persistence.CategoryJpaEntity;
import com.capgemini.training.systemapi.adapter.out.persistence.CategoryJpaRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;

/** Day 8 — Category CRUD controller. */
@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Tag(name = "Categories")
public class CategoryController {
    private final CategoryJpaRepository repo;

    @GetMapping
    @Operation(summary = "List all categories")
    public List<CategoryJpaEntity> list() { return repo.findAll(); }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryJpaEntity> getById(@PathVariable Long id) {
        return repo.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<CategoryJpaEntity> create(@RequestBody CategoryRequest req) {
        CategoryJpaEntity saved = repo.save(CategoryJpaEntity.builder()
            .name(req.name()).description(req.description()).build());
        return ResponseEntity.created(URI.create("/api/v1/categories/" + saved.getId())).body(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!repo.existsById(id)) return ResponseEntity.notFound().build();
        repo.deleteById(id); return ResponseEntity.noContent().build();
    }

    public record CategoryRequest(@NotBlank @Size(max=100) String name, @Size(max=500) String description) {}
}
