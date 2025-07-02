import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { trigger, transition, style, animate } from '@angular/animations';

@Component({
  selector: 'app-faq',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './faq.component.html',
  animations: [
    trigger('fadeSlide', [
      transition(':enter', [
        style({ height: '0', opacity: 0 }),
        animate('150ms ease-out', style({ height: '*', opacity: 1 }))
      ]),
      transition(':leave', [
        animate('150ms ease-in', style({ height: '0', opacity: 0 }))
      ])
    ])
  ]
})
export class FaqComponent {
  openFaq: number | null = null;

  toggle(id: number): void {
    this.openFaq = this.openFaq === id ? null : id;
  }

  isOpen(id: number): boolean {
    return this.openFaq === id;
  }
}
