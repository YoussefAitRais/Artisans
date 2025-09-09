import { Component } from '@angular/core';
import { EngagementsComponent } from '../../shared/engagements/engagements.component';

@Component({
  standalone: true,
  selector: 'app-artisan-engagements',
  imports: [EngagementsComponent],
  template: `
    <div class="p-4 space-y-4">
      <div class="bg-gradient-to-r from-gray-800 to-pink-700 rounded-xl p-6 text-white">
        <h1 class="text-2xl font-bold mb-2">📋 My Engagements</h1>
        <p class="text-gray-200">Manage your project engagements and work schedule</p>
      </div>
      
      <div class="bg-white rounded-xl shadow-sm border border-gray-200">
        <app-engagements [userRole]="'ARTISAN'"></app-engagements>
      </div>
    </div>
  `
})
export class ArtisanEngagementsComponent {}