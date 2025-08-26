import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ArtisanApiService, AvailabilitySlot } from '../../../services/api/artisan-api.service';

@Component({
  standalone: true,
  selector: 'app-artisan-availability',
  imports: [CommonModule, FormsModule],
  templateUrl: './availability.component.html'
})
export class ArtisanAvailabilityComponent implements OnInit {
  private api = inject(ArtisanApiService);

  slots: AvailabilitySlot[] = [];
  adding = false;

  // form draft مطابق للتايب: dayOfWeek / startTime / endTime
  draft: Omit<AvailabilitySlot, 'id'> = {
    dayOfWeek: 1,
    startTime: '09:00',
    endTime: '13:00'
  };

  ngOnInit() { this.refresh(); }

  refresh() {
    this.api.listAvailability().subscribe({
      next: s => this.slots = s ?? []
    });
  }

  dayLabel(d: number) {
    // 1..7
    return ['Lundi','Mardi','Mercredi','Jeudi','Vendredi','Samedi','Dimanche'][d - 1] || d;
    // إذا كان الباك كيرجع 0..6 بدّل ل: [d] مباشرة أو +1 حسب الحاجة
  }

  add() {
    this.adding = true;
    this.api.addAvailability(this.draft).subscribe({
      next: () => {
        this.draft = { dayOfWeek: 1, startTime: '09:00', endTime: '13:00' };
        this.refresh();
      },
      error: e => alert(e?.error?.message || 'Erreur'),
      complete: () => this.adding = false
    });
  }

  remove(id?: number) {
    if (id == null) return;
    if (!confirm('Supprimer ce créneau ?')) return;
    this.api.deleteAvailability(id).subscribe({
      next: () => this.refresh(),
      error: e => alert(e?.error?.message || 'Erreur')
    });
  }

  trackById = (_: number, s: AvailabilitySlot) => s.id;
}
