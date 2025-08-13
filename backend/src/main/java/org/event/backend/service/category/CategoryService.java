package org.event.backend.service.category;

import org.event.backend.dto.category.CategoryCreateRequest;
import org.event.backend.dto.category.CategoryResponse;
import org.event.backend.dto.category.CategoryUpdateRequest;
import org.event.backend.entity.Category;
import org.event.backend.repository.CategoryRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Business logic for categories (create, read, update, delete).
 */
@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    // Create
    @Transactional
    public CategoryResponse create(CategoryCreateRequest req) {
        String name = req.getName().trim();
        if (categoryRepository.existsByNameIgnoreCase(name)) {
            throw new DataIntegrityViolationException("Category name already exists");
        }
        Category saved = categoryRepository.save(new Category(name, req.getDescription()));
        return toResponse(saved);
    }

    // Read one
    @Transactional(readOnly = true)
    public CategoryResponse getById(Long id) {
        Category c = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));
        return toResponse(c);
    }

    // Read all (paged)
    @Transactional(readOnly = true)
    public Page<CategoryResponse> getAll(Pageable pageable) {
        return categoryRepository.findAll(pageable).map(this::toResponse);
    }

    // Update
    @Transactional
    public CategoryResponse update(Long id, CategoryUpdateRequest req) {
        Category c = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));

        String newName = req.getName().trim();
        // If name changed, ensure uniqueness
        if (!c.getName().equalsIgnoreCase(newName) && categoryRepository.existsByNameIgnoreCase(newName)) {
            throw new DataIntegrityViolationException("Category name already exists");
        }
        c.setName(newName);
        c.setDescription(req.getDescription());
        return toResponse(c);
    }

    // Delete
    @Transactional
    public void delete(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new IllegalArgumentException("Category not found");
        }
        categoryRepository.deleteById(id);
    }

    // Mapper
    private CategoryResponse toResponse(Category c) {
        return new CategoryResponse(c.getId(), c.getName(), c.getDescription());
    }
}
