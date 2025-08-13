package org.event.backend.controller;

import jakarta.validation.Valid;
import org.event.backend.dto.category.CategoryCreateRequest;
import org.event.backend.dto.category.CategoryResponse;
import org.event.backend.dto.category.CategoryUpdateRequest;
import org.event.backend.service.category.CategoryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Public read endpoints + Admin-only write endpoints (secured by /api/admin/** in SecurityConfig).
 */
@RestController
public class CategoryController {

    private final CategoryService categoryService;
    public CategoryController(CategoryService categoryService) { this.categoryService = categoryService; }

    // ---------- Public ----------
    @GetMapping("/api/categories")
    public ResponseEntity<Page<CategoryResponse>> getAll(Pageable pageable) {
        return ResponseEntity.ok(categoryService.getAll(pageable));
    }

    @GetMapping("/api/categories/{id}")
    public ResponseEntity<CategoryResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.getById(id));
    }

    // ---------- Admin ----------
    @PostMapping("/api/admin/categories")
    public ResponseEntity<CategoryResponse> create(@Valid @RequestBody CategoryCreateRequest req) {
        return ResponseEntity.ok(categoryService.create(req));
    }

    @PutMapping("/api/admin/categories/{id}")
    public ResponseEntity<CategoryResponse> update(@PathVariable Long id,
                                                   @Valid @RequestBody CategoryUpdateRequest req) {
        return ResponseEntity.ok(categoryService.update(id, req));
    }

    @DeleteMapping("/api/admin/categories/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
