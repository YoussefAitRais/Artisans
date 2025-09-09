import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { PLATFORM_ID } from '@angular/core';

import { CardsComponent } from './cards.component';

describe('CardsComponent', () => {
  let component: CardsComponent;
  let fixture: ComponentFixture<CardsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CardsComponent],
      providers: [
        { provide: PLATFORM_ID, useValue: 'browser' }
      ]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(CardsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should have 8 services', () => {
    expect(component.services.length).toBe(8);
  });

  it('should start with slide 0', () => {
    expect(component.currentSlide).toBe(0);
  });

  it('should navigate to next slide', () => {
    const initialSlide = component.currentSlide;
    component.nextSlide();
    expect(component.currentSlide).toBe(initialSlide + 1);
  });

  it('should navigate to previous slide', () => {
    component.currentSlide = 2;
    component.previousSlide();
    expect(component.currentSlide).toBe(1);
  });

  it('should loop to first slide when reaching the end', () => {
    const maxSlide = component.getMaxSlide();
    component.currentSlide = maxSlide;
    component.nextSlide();
    expect(component.currentSlide).toBe(0);
  });

  it('should auto-play by default', () => {
    expect(component.isAutoPlaying).toBe(true);
  });

  it('should toggle auto-play', () => {
    const initialState = component.isAutoPlaying;
    component.toggleAutoPlay();
    expect(component.isAutoPlaying).toBe(!initialState);
  });

  it('should calculate slides per view correctly for mobile', () => {
    spyOnProperty(window, 'innerWidth', 'get').and.returnValue(500);
    component.calculateSlidesPerView();
    expect(component.slidesPerView).toBe(1);
    expect(component.slideWidth).toBe(100);
  });

  it('should calculate slides per view correctly for desktop', () => {
    spyOnProperty(window, 'innerWidth', 'get').and.returnValue(1200);
    component.calculateSlidesPerView();
    expect(component.slidesPerView).toBe(4);
    expect(component.slideWidth).toBe(25);
  });
});
