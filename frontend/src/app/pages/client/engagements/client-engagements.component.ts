import { Component } from '@angular/core';
import { EngagementsComponent } from '../../shared/engagements/engagements.component';

@Component({
  standalone: true,
  selector: 'app-client-engagements',
  imports: [EngagementsComponent],
  template: `
    <div class="p-4 space-y-4">
      <div class="bg-gradient-to-r from-gray-800 to-pink-700 rounded-xl p-6 text-white">
        <h1 class="text-2xl font-bold mb-2">📋 My Engagements</h1>
        <p class="text-gray-200">Track your active projects and manage work progress</p>
      </div>
      
      <div class="bg-white rounded-xl shadow-sm border border-gray-200">
        <app-engagements [userRole]="'CLIENT'"></app-engagements>
      </div>
    </div>
  `
})
export class ClientEngagementsComponent {}