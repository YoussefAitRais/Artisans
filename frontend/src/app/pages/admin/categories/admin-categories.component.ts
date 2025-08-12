import { Component, computed, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

type CatStatus = 'ACTIVE' | 'INACTIVE';

export interface Category {
  id: number;
  name: string;
  description: string;
  status: CatStatus;
  createdAt: string; // ISO
}

@Component({
  selector: 'app-admin-categories',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-categories.component.html'
})
export class AdminCategoriesComponent {
  // data (mock)
  categories = signal<Category[]>([
    { id: 1, name: 'Plomberie', description: 'Services de plomberie', status: 'ACTIVE', createdAt: '2024-01-12' },
    { id: 2, name: 'Électricité', description: 'Installations électriques', status: 'ACTIVE', createdAt: '2024-02-20' },
    { id: 3, name: 'Peinture', description: 'Peinture intérieure/extérieure', status: 'INACTIVE', createdAt: '2024-03-11' },
    { id: 4, name: 'Mécanique', description: 'Réparation automobile', status: 'ACTIVE', createdAt: '2024-04-05' },
  ]);

  q = signal('');
  status = signal<CatStatus | 'ALL'>('ALL');
  page = signal(1);
  size = signal(6);

  filtered = computed(() => {
    const text = this.q().toLowerCase();
    const st = this.status();
    return this.categories().filter(c => {
      const t = [c.name, c.description, c.status].join(' ').toLowerCase().includes(text);
      const s = st === 'ALL' || c.status === st;
      return t && s;
    });
  });

  totalPages = computed(() => Math.max(1, Math.ceil(this.filtered().length / this.size())));
  paged = computed(() => {
    const start = (this.page() - 1) * this.size();
    return this.filtered().slice(start, start + this.size());
  });

  // actions
  toggle(c: Category) {
    const next: CatStatus = c.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE';
    this.categories.update(list => list.map(x => x.id === c.id ? { ...x, status: next } : x));
  }
  remove(c: Category) {
    if (!confirm(`Supprimer "${c.name}" ?`)) return;
    this.categories.update(list => list.filter(x => x.id !== c.id));
  }

  // modal
  show = signal(false);
  draft = signal<Category | null>(null);

  newCategory() {
    const id = Math.max(...this.categories().map(c => c.id), 0) + 1;
    this.draft.set({ id, name: '', description: '', status: 'ACTIVE', createdAt: new Date().toISOString().slice(0,10) });
    this.show.set(true);
  }
  edit(c: Category) {
    this.draft.set({ ...c });
    this.show.set(true);
  }
  save() {
    const d = this.draft();
    if (!d || !d.name.trim()) return alert('Nom requis');
    this.categories.update(list => {
      const exists = list.some(x => x.id === d.id);
      return exists ? list.map(x => x.id === d.id ? d : x) : [d, ...list];
    });
    this.show.set(false);
  }
  close() { this.show.set(false); }

  trackById = (_: number, c: Category) => c.id;
  prev() { this.page.set(Math.max(1, this.page() - 1)); }
  next() { this.page.set(Math.min(this.totalPages(), this.page() + 1)); }
}
