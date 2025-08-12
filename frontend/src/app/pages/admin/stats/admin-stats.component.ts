import { Component, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-admin-stats',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './admin-stats.component.html'
})
export class AdminStatsComponent {
  totals = { users: 1240, artisans: 320, categories: 12, reviews: 980 };

  months = ['Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug'];
  orders = signal([45, 60, 55, 80, 120, 100]);
  signups = signal([20, 30, 25, 35, 40, 50]);

  // مقياس الرسم الصغير
  maxY = 140; // القيمة القصوى للبيانات

  // ✅ حساب نقاط الخط هنا (بدون backticks في HTML)
  points = computed(() =>
    this.signups()
      .map((v, i) => {
        const x = i * 40 + 20;              // المسافة الأفقية أصغر
        const y = 100 - (v / this.maxY) * 80; // ارتفاع SVG 100 مع padding 20
        return `${x},${y}`;
      })
      .join(' ')
  );

  // مراكز النقاط
  cx(i: number) { return i * 40 + 20; }
  cy(v: number) { return 100 - (v / this.maxY) * 80; }
}
