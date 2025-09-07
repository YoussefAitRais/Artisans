import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ArtisanApiService, AvailabilitySlot } from '../../../services/api/artisan-api.service';

@Component({
  standalone: true,
  selector: 'app-artisan-availability',
  template: `
  <div class="p-4 space-y-4">
    <h2 class="text-xl font-bold">Disponibilités</h2>

    <div class="grid gap-2 md:grid-cols-4">
      <select [(ngModel)]="draft.dayOfWeek" class="border rounded p-2">
        <option [ngValue]="1">Mon</option><option [ngValue]="2">Tue</option>
        <option [ngValue]="3">Wed</option><option [ngValue]="4">Thu</option>
        <option [ngValue]="5">Fri</option><option [ngValue]="6">Sat</option>
        <option [ngValue]="7">Sun</option>
      </select>
      <input type="time" [(ngModel)]="draft.startTime" class="border rounded p-2" />
      <input type="time" [(ngModel)]="draft.endTime" class="border rounded p-2" />
      <button class="bg-sky-600 text-white rounded px-3 py-2" (click)="add()">Ajouter</button>
    </div>

    <div *ngIf="loading">Loading…</div>

    <ul class="space-y-2" *ngIf="!loading">
      <li *ngFor="let s of slots">
        <span class="text-sm">{{ s.dayOfWeek }} {{ s.startTime }}-{{ s.endTime }}</span>
        <button class="ml-2 border rounded px-2 py-0.5" (click)="remove(s.id!)">Supprimer</button>
      </li>
    </ul>
  </div>`,
  imports: [CommonModule, FormsModule]
})
export class ArtisanAvailabilityComponent implements OnInit {
  private api = inject(ArtisanApiService);

  slots: AvailabilitySlot[] = [];
  loading = false;

  draft: AvailabilitySlot = { dayOfWeek: 1, startTime: '09:00', endTime: '17:00' };

  ngOnInit(): void { this.reload(); }

  reload(): void {
    this.loading = true;
    this.api.listAvailability().subscribe({
      next: (x: AvailabilitySlot[]) => (this.slots = x),
      complete: () => (this.loading = false)
    });
  }

  add(): void {
    this.api.addAvailability(this.draft).subscribe({
      next: (s: AvailabilitySlot) => this.slots.push(s),
      complete: () => (this.draft = { dayOfWeek: 1, startTime: '09:00', endTime: '17:00' })
    });
  }

  remove(id: number): void {
    this.api.deleteAvailability(id).subscribe(() => {
      this.slots = this.slots.filter(x => x.id !== id);
    });
  }
}
