import { Component, inject } from '@angular/core';
import { MessagingComponent } from '../../shared/messaging/messaging.component';
import { MessageResponse, MessageSenderRole } from '../../../services/api/messaging-api.service';

@Component({
  standalone: true,
  selector: 'app-client-messaging',
  imports: [MessagingComponent],
  templateUrl: './client-messaging.component.html'
})
export class ClientMessagingComponent {}