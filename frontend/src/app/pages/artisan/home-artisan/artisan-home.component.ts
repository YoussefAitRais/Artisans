import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { Page, Quote, ArtisanApiService, Review, ArtisanInboxItem } from '../../../services/api/artisan-api.service';

/**
 * Type definition for dashboard statistics
 * Clean Code: Using clear type definitions helps beginners understand data structure
 */
type DashboardStat = { 
  key: string;      // Unique identifier for the statistic
  label: string;    // Display name shown to user
  value: number;    // The actual numeric value
};

/**
 * ArtisanHomeComponent - Clean Code Example for Beginners
 * 
 * This component demonstrates:
 * - Clear component structure
 * - Proper dependency injection
 * - Simple data handling
 * - User-friendly error handling
 * - Accessible design patterns
 */
@Component({
  selector: 'app-artisan-home',
  templateUrl: './artisan-home.component.html',
  standalone: true,
  imports: [CommonModule, RouterModule], // Only import what we need
})
export class ArtisanHomeComponent implements OnInit {
  
  // === COMPONENT DATA ===
  
  /**
   * Dashboard statistics to display in cards
   * Initialized with default values to prevent errors
   */
  stats: DashboardStat[] = [
    { key: 'inboxResponded', label: 'Demandes répondues', value: 0 },
    { key: 'sentQuotes',     label: 'Devis envoyés',       value: 0 },
    { key: 'avgRating',      label: 'Note moyenne',        value: 0 },
  ];

  /**
   * Recent reviews to show in the dashboard
   * Start with empty array to prevent template errors
   */
  recentReviews: Review[] = [];

  /**
   * Loading state to show user when data is being fetched
   */
  isLoading = true;

  /**
   * Error message to show if something goes wrong
   */
  errorMessage: string | null = null;

  // === CONSTRUCTOR ===
  
  /**
   * Constructor with dependency injection
   * Clean Code: Use readonly for injected services to prevent accidental changes
   */
  constructor(private readonly api: ArtisanApiService) {}

  // === HELPER METHODS ===

  /**
   * Updates a specific statistic value
   * 
   * Clean Code Principle: Small, focused methods that do one thing well
   * 
   * @param key - The key of the stat to update
   * @param value - The new value to set
   */
  private updateStatistic(key: string, value: number): void {
    const statistic = this.stats.find((stat) => stat.key === key);
    if (statistic) {
      statistic.value = value;
    }
  }

  /**
   * Converts a numeric rating to star emojis
   * 
   * This method is called from the template to display ratings visually.
   * Using a method instead of string repetition makes it more maintainable.
   * 
   * @param rating - Number of stars (0-5)
   * @return String of star emojis
   */
  getStarRating(rating: number): string {
    // Ensure rating is within valid range (0-5)
    const validRating = Math.max(0, Math.min(5, Math.floor(rating)));
    return '⭐'.repeat(validRating);
  }

  /**
   * Handles errors in a user-friendly way
   * 
   * Clean Code: Centralized error handling for consistency
   * 
   * @param error - The error that occurred
   * @param operation - Description of what was being attempted
   */
  private handleError(error: any, operation: string): void {
    console.error(`Error during ${operation}:`, error);
    this.errorMessage = `Erreur lors du chargement des ${operation}. Veuillez rafraîchir la page.`;
    this.isLoading = false;
  }

  // === LIFECYCLE HOOKS ===

  /**
   * Component initialization - loads all dashboard data
   * 
   * Clean Code Approach:
   * 1. Start loading state
   * 2. Load each data type separately
   * 3. Handle errors gracefully
   * 4. Update UI when complete
   */
  ngOnInit(): void {
    this.isLoading = true;
    this.errorMessage = null;
    
    // Load all dashboard data
    this.loadInboxStatistics();
    this.loadQuoteStatistics();
    this.loadReviewData();
  }

  // === DATA LOADING METHODS ===

  /**
   * Loads inbox statistics (responded requests)
   * 
   * Fetches inbox data and counts how many requests have been responded to.
   */
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

  /**
   * Loads quote statistics (total quotes sent)
   * 
   * Gets the total number of quotes this artisan has sent.
   */
  private loadQuoteStatistics(): void {
    this.api.listQuotes().subscribe({
      next: (page: Page<Quote>) => {
        const totalQuotes = page.totalElements ?? page.content.length;
        this.updateStatistic('sentQuotes', totalQuotes);
      },
      error: (error) => this.handleError(error, 'devis')
    });
  }

  /**
   * Loads review data (recent reviews and average rating)
   * 
   * Gets reviews and calculates average rating for display.
   */
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
