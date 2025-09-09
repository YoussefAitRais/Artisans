import { Component, OnInit, inject, signal, computed, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { EngagementApiService, EngagementResponse, EngagementStatus, Page, EngagementConfirmRequest } from '../../../services/api/engagement-api.service';
import { MessagingApiService } from '../../../services/api/messaging-api.service';

@Component({
  standalone: true,
  selector: 'app-engagements',
  imports: [CommonModule, FormsModule],
  templateUrl: './engagements.component.html'
})
export class EngagementsComponent implements OnInit {
  @Input() userRole: 'CLIENT' | 'ARTISAN' = 'CLIENT';
  
  private api = inject(EngagementApiService);
  private messagingApi = inject(MessagingApiService);
  private router = inject(Router);

  // Loading states
  loading = signal(false);
  actionLoading = signal<number | null>(null);

  // Data
  engagements = signal<EngagementResponse[]>([]);
  selectedEngagement = signal<EngagementResponse | null>(null);
  
  // Pagination
  currentPage = signal(0);
  totalPages = signal(0);

  // Form for confirmation
  confirmForm = signal<{startDate: string, endDate: string}>({
    startDate: '',
    endDate: ''
  });
  showConfirmModal = signal(false);
  error = signal<string | null>(null);

  // Computed values
  hasEngagements = computed(() => this.engagements().length > 0);

  ngOnInit() {
    this.loadEngagements();
  }

  // Update form methods
  updateStartDate(date: string) {
    this.confirmForm.update(form => ({...form, startDate: date}));
  }

  updateEndDate(date: string) {
    this.confirmForm.update(form => ({...form, endDate: date}));
  }

  // Load engagements list
  loadEngagements() {
    this.loading.set(true);
    this.error.set(null);

    this.api.myEngagements(this.currentPage(), 10).subscribe({
      next: (page: Page<EngagementResponse>) => {
        this.engagements.set(page.content);
        this.totalPages.set(page.totalPages);
        this.loading.set(false);
      },
      error: (err: any) => {
        console.error('Error loading engagements:', err);
        this.error.set('Unable to load engagements. Please try again.');
        this.loading.set(false);
      }
    });
  }

  // Get status badge color
  getStatusColor(status: EngagementStatus): string {
    switch (status) {
      case 'PENDING_CONFIRMATION': return 'bg-yellow-100 text-yellow-800';
      case 'SCHEDULED': return 'bg-blue-100 text-blue-800';
      case 'IN_PROGRESS': return 'bg-green-100 text-green-800';
      case 'COMPLETED': return 'bg-gray-100 text-gray-800';
      case 'CANCELLED': return 'bg-red-100 text-red-800';
      default: return 'bg-gray-100 text-gray-800';
    }
  }

  // Get status display text
  getStatusText(status: EngagementStatus): string {
    switch (status) {
      case 'PENDING_CONFIRMATION': return 'Awaiting Confirmation';
      case 'SCHEDULED': return 'Scheduled';
      case 'IN_PROGRESS': return 'In Progress';
      case 'COMPLETED': return 'Completed';
      case 'CANCELLED': return 'Cancelled';
      default: return status;
    }
  }

  // Check if action is available for current user and status
  canConfirm(engagement: EngagementResponse): boolean {
    return this.userRole === 'ARTISAN' && engagement.status === 'PENDING_CONFIRMATION';
  }

  canStart(engagement: EngagementResponse): boolean {
    return engagement.status === 'SCHEDULED';
  }

  canComplete(engagement: EngagementResponse): boolean {
    return this.userRole === 'CLIENT' && engagement.status === 'IN_PROGRESS';
  }

  canCancel(engagement: EngagementResponse): boolean {
    return engagement.status === 'PENDING_CONFIRMATION' || engagement.status === 'SCHEDULED';
  }

  // Actions
  openConfirmModal(engagement: EngagementResponse) {
    this.selectedEngagement.set(engagement);
    this.confirmForm.set({ startDate: '', endDate: '' });
    this.showConfirmModal.set(true);
    this.error.set(null);
  }

  closeConfirmModal() {
    this.showConfirmModal.set(false);
    this.selectedEngagement.set(null);
  }

  confirmEngagement() {
    const engagement = this.selectedEngagement();
    const form = this.confirmForm();
    
    if (!engagement || !form.startDate || !form.endDate) {
      this.error.set('Please fill in all required fields.');
      return;
    }

    if (new Date(form.startDate) >= new Date(form.endDate)) {
      this.error.set('Start date must be before end date.');
      return;
    }

    this.actionLoading.set(engagement.id);
    this.error.set(null);

    const request: EngagementConfirmRequest = {
      startDate: form.startDate,
      endDate: form.endDate
    };

    this.api.confirmAsArtisan(engagement.id, request).subscribe({
      next: (updated) => {
        console.log('Engagement confirmed:', updated);
        this.updateEngagementInList(updated);
        this.closeConfirmModal();
        this.actionLoading.set(null);
      },
      error: (err: any) => {
        console.error('Error confirming engagement:', err);
        this.error.set('Unable to confirm engagement. Please try again.');
        this.actionLoading.set(null);
      }
    });
  }

  startEngagement(engagement: EngagementResponse) {
    this.actionLoading.set(engagement.id);
    this.error.set(null);

    this.api.startEngagement(engagement.id).subscribe({
      next: (updated) => {
        console.log('Engagement started:', updated);
        this.updateEngagementInList(updated);
        this.actionLoading.set(null);
      },
      error: (err: any) => {
        console.error('Error starting engagement:', err);
        this.error.set('Unable to start engagement. Please try again.');
        this.actionLoading.set(null);
      }
    });
  }

  completeEngagement(engagement: EngagementResponse) {
    this.actionLoading.set(engagement.id);
    this.error.set(null);

    this.api.completeEngagement(engagement.id).subscribe({
      next: (updated) => {
        console.log('Engagement completed:', updated);
        this.updateEngagementInList(updated);
        this.actionLoading.set(null);
      },
      error: (err: any) => {
        console.error('Error completing engagement:', err);
        this.error.set('Unable to complete engagement. Please try again.');
        this.actionLoading.set(null);
      }
    });
  }

  cancelEngagement(engagement: EngagementResponse) {
    if (!confirm('Are you sure you want to cancel this engagement?')) {
      return;
    }

    this.actionLoading.set(engagement.id);
    this.error.set(null);

    this.api.cancelEngagement(engagement.id).subscribe({
      next: (updated) => {
        console.log('Engagement cancelled:', updated);
        this.updateEngagementInList(updated);
        this.actionLoading.set(null);
      },
      error: (err: any) => {
        console.error('Error cancelling engagement:', err);
        this.error.set('Unable to cancel engagement. Please try again.');
        this.actionLoading.set(null);
      }
    });
  }

  openMessages(engagement: EngagementResponse) {
    // Create conversation for this engagement and navigate to messages
    this.messagingApi.getOrCreateByEngagement(engagement.id).subscribe({
      next: () => {
        const route = this.userRole === 'CLIENT' ? '/client/messages' : '/artisan/messages';
        this.router.navigate([route]);
      },
      error: (err: any) => {
        console.error('Error opening messages:', err);
        this.error.set('Unable to open messages. Please try again.');
      }
    });
  }

  // Helper methods
  private updateEngagementInList(updated: EngagementResponse) {
    this.engagements.update(engagements => 
      engagements.map(e => e.id === updated.id ? updated : e)
    );
  }

  formatDate(dateStr: string): string {
    return new Date(dateStr).toLocaleDateString();
  }

  formatPrice(price: number): string {
    return new Intl.NumberFormat('fr-FR', {
      style: 'currency',
      currency: 'EUR'
    }).format(price);
  }

  // Pagination
  nextPage() {
    if (this.currentPage() + 1 < this.totalPages()) {
      this.currentPage.update(page => page + 1);
      this.loadEngagements();
    }
  }

  prevPage() {
    if (this.currentPage() > 0) {
      this.currentPage.update(page => page - 1);
      this.loadEngagements();
    }
  }

  // TrackBy function for performance
  trackEngagementById(index: number, engagement: EngagementResponse): number {
    return engagement.id;
  }
}