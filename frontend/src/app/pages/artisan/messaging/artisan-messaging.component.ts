import { Component, inject } from '@angular/core';
import { MessagingComponent } from '../../shared/messaging/messaging.component';
import { MessageResponse, MessageSenderRole } from '../../../services/api/messaging-api.service';

@Component({
  standalone: true,
  selector: 'app-artisan-messaging',
  imports: [MessagingComponent],
  templateUrl: './artisan-messaging.component.html'
})
export class ArtisanMessagingComponent {}