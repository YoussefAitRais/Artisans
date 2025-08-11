import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ArtisanDashboardComponent } from './artisan-dashboard.component';

describe('ArtisanDashboardComponent', () => {
  let component: ArtisanDashboardComponent;
  let fixture: ComponentFixture<ArtisanDashboardComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ArtisanDashboardComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(ArtisanDashboardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
