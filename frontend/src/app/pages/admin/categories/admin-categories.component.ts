import { Component, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import {
  AdminApi, CategoryCreateRequest, CategoryResponse, CategoryUpdateRequest, Page
} from '../../../services/api/admin-api.service';

@Component({
  selector: 'app-admin-categories',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-categories.component.html'
})
export class AdminCategoriesComponent implements OnInit {
  // paging
  page = signal(0);
  size = signal(10);
  sort = signal('id,desc');

  // server page
  data = signal<Page<CategoryResponse> | null>(null);
  loading = signal(false);
  error = signal('');

  // create form
  newName = signal('');
  newDesc = signal('');

  // edit inline
  editingId = signal<number | null>(null);
  editName = signal('');
  editDesc = signal('');

  totalPages = computed(() => this.data()?.totalPages ?? 1);

  constructor(private api: AdminApi) {}

  ngOnInit(): void {
    this.load();
  }

  load() {
    this.loading.set(true); this.error.set('');
    this.api.categories(this.page(), this.size(), this.sort()).subscribe({
      next: (p) => this.data.set(p),
      error: (e) => this.error.set(e?.error?.message || 'Erreur de chargement'),
      complete: () => this.loading.set(false)
    });
  }

  // Create
  create() {
    const body: CategoryCreateRequest = {
      name: this.newName().trim(),
      description: this.newDesc().trim() || undefined
    };
    if (!body.name) return;
    this.api.createCategory(body).subscribe({
      next: () => { this.newName.set(''); this.newDesc.set(''); this.load(); }
    });
  }

  // Edit
  startEdit(c: CategoryResponse) {
    this.editingId.set(c.id);
    this.editName.set(c.name);
    this.editDesc.set(c.description ?? '');
  }
  cancelEdit() { this.editingId.set(null); }

  saveEdit(id: number) {
    const body: CategoryUpdateRequest = {
      name: this.editName().trim(),
      description: this.editDesc().trim() || undefined
    };
    if (!body.name) return;
    this.api.updateCategory(id, body).subscribe({
      next: () => { this.editingId.set(null); this.load(); }
    });
  }

  // Delete
  remove(id: number) {
    if (!confirm('Supprimer cette catégorie ?')) return;
    this.api.deleteCategory(id).subscribe({ next: () => this.load() });
  }

  prev() { this.page.set(Math.max(0, this.page() - 1)); this.load(); }
  next() {
    const last = Math.max(0, (this.data()?.totalPages ?? 1) - 1);
    this.page.set(Math.min(last, this.page() + 1));
    this.load();
  }
}
