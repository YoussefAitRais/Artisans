import { Component, OnInit, OnDestroy, ElementRef, ViewChild, Inject, PLATFORM_ID } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';

@Component({
  selector: 'app-cards',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './cards.component.html',
  styleUrls: ['./cards.component.css']
})
export class CardsComponent implements OnInit, OnDestroy {
  @ViewChild('carouselContainer', { static: false }) carouselContainer!: ElementRef;

  // Service data with enhanced information
  services = [
    { name: 'Forgeron', image: 'assets/Images/artisan.jpg', category: 'Métallurgie', projectCount: 45 },
    { name: 'Électricien', image: 'assets/Images/artisan.jpg', category: 'Électricité', projectCount: 52 },
    { name: 'Ouvrier du bâtiment', image: 'assets/Images/artisan.jpg', category: 'Construction', projectCount: 38 },
    { name: 'Plombier', image: 'assets/Images/artisan.jpg', category: 'Plomberie', projectCount: 41 },
    { name: 'Femme de ménage', image: 'assets/Images/artisan.jpg', category: 'Nettoyage', projectCount: 29 },
    { name: 'Technicien climatisation', image: 'assets/Images/artisan.jpg', category: 'Climatisation', projectCount: 33 },
    { name: 'Peintre', image: 'assets/Images/artisan.jpg', category: 'Décoration', projectCount: 47 },
    { name: 'Mécanicien', image: 'assets/Images/artisan.jpg', category: 'Mécanique', projectCount: 36 },
  ];

  // Carousel state
  currentSlide = 0;
  slidesPerView = 4; // Default for large screens
  slideWidth = 25; // 100% / 4 slides
  isAutoPlaying = true;
  autoPlayInterval: any;
  isBrowser: boolean;

  constructor(@Inject(PLATFORM_ID) private platformId: Object) {
    this.isBrowser = isPlatformBrowser(this.platformId);
  }

  ngOnInit(): void {
    if (this.isBrowser) {
      this.calculateSlidesPerView();
      this.startAutoPlay();

      // Listen for window resize
      window.addEventListener('resize', () => this.calculateSlidesPerView());
    }
  }

  ngOnDestroy(): void {
    if (this.autoPlayInterval) {
      clearInterval(this.autoPlayInterval);
    }
    if (this.isBrowser) {
      window.removeEventListener('resize', () => this.calculateSlidesPerView());
    }
  }


  calculateSlidesPerView(): void {
    if (!this.isBrowser) return;

    const width = window.innerWidth;

    if (width < 640) { // Mobile
      this.slidesPerView = 1;
      this.slideWidth = 100;
    } else if (width < 768) { // Small tablet
      this.slidesPerView = 2;
      this.slideWidth = 50;
    } else if (width < 1024) { // Tablet
      this.slidesPerView = 3;
      this.slideWidth = 33.333;
    } else { // Desktop
      this.slidesPerView = 4;
      this.slideWidth = 25;
    }

    // Ensure current slide is within bounds
    const maxSlide = this.getMaxSlide();
    if (this.currentSlide > maxSlide) {
      this.currentSlide = maxSlide;
    }
  }


  getMaxSlide(): number {
    return Math.max(0, this.services.length - this.slidesPerView);
  }


  getTotalSlides(): number[] {
    const totalSlides = Math.ceil(this.services.length / this.slidesPerView);
    return Array(totalSlides).fill(0);
  }


  nextSlide(): void {
    const maxSlide = this.getMaxSlide();
    this.currentSlide = this.currentSlide >= maxSlide ? 0 : this.currentSlide + 1;
  }


  previousSlide(): void {
    const maxSlide = this.getMaxSlide();
    this.currentSlide = this.currentSlide <= 0 ? maxSlide : this.currentSlide - 1;
  }


  goToSlide(slideIndex: number): void {
    const maxSlide = this.getMaxSlide();
    this.currentSlide = Math.min(slideIndex, maxSlide);
  }


  startAutoPlay(): void {
    if (!this.isBrowser || !this.isAutoPlaying) return;

    this.autoPlayInterval = setInterval(() => {
      this.nextSlide();
    }, 4000); // Change slide every 4 seconds
  }


  stopAutoPlay(): void {
    if (this.autoPlayInterval) {
      clearInterval(this.autoPlayInterval);
      this.autoPlayInterval = null;
    }
  }


  toggleAutoPlay(): void {
    this.isAutoPlaying = !this.isAutoPlaying;

    if (this.isAutoPlaying) {
      this.startAutoPlay();
    } else {
      this.stopAutoPlay();
    }
  }


  getServiceProjectCount(service: any): number {
    return service.projectCount || 0;
  }


  onMouseEnter(): void {
    this.stopAutoPlay();
  }


  onMouseLeave(): void {
    if (this.isAutoPlaying) {
      this.startAutoPlay();
    }
  }
}
