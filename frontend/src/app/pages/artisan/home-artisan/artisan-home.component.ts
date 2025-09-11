import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { Page, Quote, ArtisanApiService, Review, ArtisanInboxItem } from '../../../services/api/artisan-api.service';

type DashboardStat = {
  key: string;      // Unique identifier for the statistic
  label: string;    // Display name shown to user
  value: number;    // The actual numeric value
};


@Component({
  selector: 'app-artisan-home',
  templateUrl: './artisan-home.component.html',
  standalone: true,
  imports: [CommonModule, RouterModule], // Only import what we need
})
export class ArtisanHomeComponent implements OnInit {

  // === COMPONENT DATA ===


  stats: DashboardStat[] = [
    { key: 'inboxResponded', label: 'Demandes répondues', value: 0 },
    { key: 'sentQuotes',     label: 'Devis envoyés',       value: 0 },
    { key: 'avgRating',      label: 'Note moyenne',        value: 0 },
  ];


  recentReviews: Review[] = [];


  isLoading = true;


  errorMessage: string | null = null;

  // === CONSTRUCTOR ===


  constructor(private readonly api: ArtisanApiService) {}

  // === HELPER METHODS ===


  private updateStatistic(key: string, value: number): void {
    const statistic = this.stats.find((stat) => stat.key === key);
    if (statistic) {
      statistic.value = value;
    }
  }


  getStarRating(rating: number): string {
    // Ensure rating is within valid range (0-5)
    const validRating = Math.max(0, Math.min(5, Math.floor(rating)));
    return '⭐'.repeat(validRating);
  }


  private handleError(error: any, operation: string): void {
    console.error(`Error during ${operation}:`, error);
    this.errorMessage = `Erreur lors du chargement des ${operation}. Veuillez rafraîchir la page.`;
    this.isLoading = false;
  }

  // === LIFECYCLE HOOKS ===


  ngOnInit(): void {
    this.isLoading = true;
    this.errorMessage = null;

    // Load all dashboard data
    this.loadInboxStatistics();
    this.loadQuoteStatistics();
    this.loadReviewData();
  }

  // === DATA LOADING METHODS ===


  private loadInboxStatistics(): void {
    this.api.inbox(0, 50, 'ALL').subscribe({
      next: (page: Page<ArtisanInboxItem>) => {
        const respondedCount = page.content.filter(
          (item) => item.status === 'RESPONDED'
        ).length;
        this.updateStatistic('inboxResponded', respondedCount);
      },
      error: (error) => this.handleError(error, 'demandes')
    });
  }


  private loadQuoteStatistics(): void {
    this.api.listQuotes().subscribe({
      next: (page: Page<Quote>) => {
        const totalQuotes = page.totalElements ?? page.content.length;
        this.updateStatistic('sentQuotes', totalQuotes);
      },
      error: (error) => this.handleError(error, 'devis')
    });
  }


  private loadReviewData(): void {
    this.api.listReviews().subscribe({
      next: (page: Page<Review>) => {
        // Get the 3 most recent reviews for display
        this.recentReviews = page.content.slice(0, 3);

        // Calculate average rating
        const ratings = page.content
          .map((review) => review.rating ?? 0)
          .filter((rating) => rating > 0); // Only count valid ratings

        const averageRating = ratings.length > 0
          ? Math.round((ratings.reduce((sum, rating) => sum + rating, 0) / ratings.length) * 10) / 10
          : 0;

        this.updateStatistic('avgRating', averageRating);
        this.isLoading = false; // Data loading complete
      },
      error: (error) => {
        this.handleError(error, 'avis');
        this.isLoading = false;
      }
    });
  }
}
