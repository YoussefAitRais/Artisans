import { Component, computed, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

type Role = 'ADMIN' | 'ARTISAN' | 'CLIENT';
type Status = 'ACTIVE' | 'SUSPENDED';

export interface AdminUser {
  id: number;
  name: string;
  email: string;
  role: Role;
  status: Status;
  createdAt: string;
}

@Component({
  selector: 'app-admin-users',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-users.component.html'
})
export class AdminUsersComponent {
  users = signal<AdminUser[]>([
    { id: 1, name: 'Yassine A.', email: 'yassine@mail.com', role: 'ADMIN',  status: 'ACTIVE',    createdAt: '2024-01-15' },
    { id: 2, name: 'Salma B.',   email: 'salma@mail.com',   role: 'ARTISAN',status: 'ACTIVE',    createdAt: '2024-02-02' },
    { id: 3, name: 'Khalid C.',  email: 'khalid@mail.com',  role: 'CLIENT', status: 'SUSPENDED', createdAt: '2024-03-09' },
    { id: 4, name: 'Amine D.',   email: 'amine@mail.com',   role: 'ARTISAN',status: 'ACTIVE',    createdAt: '2024-04-20' },
    { id: 5, name: 'Nadia E.',   email: 'nadia@mail.com',   role: 'CLIENT', status: 'ACTIVE',    createdAt: '2024-05-01' },
    { id: 6, name: 'Sami F.',    email: 'sami@mail.com',    role: 'CLIENT', status: 'ACTIVE',    createdAt: '2024-06-10' },
    { id: 7, name: 'Omar G.',    email: 'omar@mail.com',    role: 'ARTISAN',status: 'SUSPENDED', createdAt: '2024-07-03' },
    { id: 8, name: 'Hiba H.',    email: 'hiba@mail.com',    role: 'CLIENT', status: 'ACTIVE',    createdAt: '2024-07-25' },
    { id: 9, name: 'Sara I.',    email: 'sara@mail.com',    role: 'CLIENT', status: 'ACTIVE',    createdAt: '2024-08-01' },
  ]);

  q = signal('');
  roleFilter = signal<Role | 'ALL'>('ALL');
  statusFilter = signal<Status | 'ALL'>('ALL');

  sortKey = signal<keyof AdminUser>('createdAt');
  sortDir = signal<'asc' | 'desc'>('desc');

  page = signal(1);
  pageSize = signal(6);

  filtered = computed(() => {
    const text = this.q().toLowerCase();
    const role = this.roleFilter();
    const status = this.statusFilter();
    const data = this.users().filter(u => {
      const matchText = !text || [u.name, u.email, u.role, u.status].join(' ').toLowerCase().includes(text);
      const matchRole = role === 'ALL' || u.role === role;
      const matchStatus = status === 'ALL' || u.status === status;
      return matchText && matchRole && matchStatus;
    });
    const dir = this.sortDir();
    const key = this.sortKey();
    return data.sort((a, b) => {
      const av: any = a[key] ?? '';
      const bv: any = b[key] ?? '';
      if (av < bv) return dir === 'asc' ? -1 : 1;
      if (av > bv) return dir === 'asc' ? 1 : -1;
      return 0;
    });
  });

  totalPages = computed(() => Math.max(1, Math.ceil(this.filtered().length / this.pageSize())));
  paged = computed(() => {
    const start = (this.page() - 1) * this.pageSize();
    return this.filtered().slice(start, start + this.pageSize());
  });

  setSort(key: keyof AdminUser) {
    if (this.sortKey() === key) this.sortDir.set(this.sortDir() === 'asc' ? 'desc' : 'asc');
    else { this.sortKey.set(key); this.sortDir.set('asc'); }
  }

  resetFilters() {
    this.q.set('');
    this.roleFilter.set('ALL');
    this.statusFilter.set('ALL');
    this.page.set(1);
  }

  toggleStatus(u: AdminUser) {
    const next: Status = u.status === 'ACTIVE' ? 'SUSPENDED' : 'ACTIVE';
    this.users.update(list => list.map(x => x.id === u.id ? { ...x, status: next } : x));
  }

  remove(u: AdminUser) {
    if (!confirm(`Supprimer ${u.name} ?`)) return;
    this.users.update(list => list.filter(x => x.id !== u.id));
  }

  // modal
  showModal = signal(false);
  draft = signal<AdminUser | null>(null);

  openEdit(u: AdminUser) {
    this.draft.set({ ...u });
    this.showModal.set(true);
  }

  newUser() {
    const id = Math.max(...this.users().map(u => u.id), 0) + 1;
    this.draft.set({ id, name: '', email: '', role: 'CLIENT', status: 'ACTIVE', createdAt: new Date().toISOString().slice(0,10) });
    this.showModal.set(true);
  }

  save() {
    const d = this.draft();
    if (!d || !d.name.trim() || !d.email.trim()) return alert('Nom et email obligatoires');
    this.users.update(list => list.some(u => u.id === d.id) ? list.map(u => u.id === d.id ? d : u) : [d, ...list]);
    this.showModal.set(false);
  }
  closeModal() { this.showModal.set(false); }

  // helpers for template
  trackById = (_: number, u: AdminUser) => u.id;
  prevPage() { this.page.set(Math.max(1, this.page() - 1)); }
  nextPage() { this.page.set(Math.min(this.totalPages(), this.page() + 1)); }
}
