import { Component } from '@angular/core';
import { CommonModule, NgFor } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  standalone: true,
  selector: 'app-artisan-availability',
  imports: [CommonModule, FormsModule, NgFor],
  templateUrl: './availability.component.html'
})
export class ArtisanAvailabilityComponent {
  slots = [
    { day: 'Lundi',    from: '09:00', to: '13:00' },
    { day: 'Mercredi', from: '14:00', to: '18:00' },
  ];
}
