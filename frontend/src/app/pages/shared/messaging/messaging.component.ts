import { Component, OnInit, inject, signal, computed, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { MessagingApiService, ConversationResponse, MessageResponse, MessageSenderRole, Page } from '../../../services/api/messaging-api.service';

@Component({
  standalone: true,
  selector: 'app-messaging',
  imports: [CommonModule, FormsModule],
  templateUrl: './messaging.component.html'
})
export class MessagingComponent implements OnInit {
  @Input() userRole: 'CLIENT' | 'ARTISAN' = 'CLIENT';
  
  private api = inject(MessagingApiService);
  private router = inject(Router);

  // Loading states
  conversationsLoading = signal(false);
  messagesLoading = signal(false);
  sending = signal(false);

  // Data
  conversations = signal<ConversationResponse[]>([]);
  currentConversation = signal<ConversationResponse | null>(null);
  messages = signal<MessageResponse[]>([]);
  
  // Pagination
  conversationsPage = signal(0);
  conversationsTotalPages = signal(0);
  messagesPage = signal(0);
  messagesTotalPages = signal(0);

  // Form
  newMessageText = signal('');
  error = signal<string | null>(null);

  // Computed values
  hasConversations = computed(() => this.conversations().length > 0);
  hasMessages = computed(() => this.messages().length > 0);
  canSendMessage = computed(() => 
    this.newMessageText().trim().length > 0 && 
    this.currentConversation() !== null && 
    !this.sending()
  );

  ngOnInit() {
    this.loadConversations();
  }

  // Load conversations list
  loadConversations() {
    this.conversationsLoading.set(true);
    this.error.set(null);

    this.api.myConversations(this.conversationsPage(), 10).subscribe({
      next: (page: Page<ConversationResponse>) => {
        this.conversations.set(page.content);
        this.conversationsTotalPages.set(page.totalPages);
        this.conversationsLoading.set(false);
      },
      error: (err: any) => {
        console.error('Error loading conversations:', err);
        this.error.set('Unable to load conversations. Please try again.');
        this.conversationsLoading.set(false);
      }
    });
  }

  // Select and load a conversation
  selectConversation(conversation: ConversationResponse) {
    this.currentConversation.set(conversation);
    this.messagesPage.set(0);
    this.loadMessages();
  }

  // Load messages for current conversation
  loadMessages() {
    const conversation = this.currentConversation();
    if (!conversation) return;

    this.messagesLoading.set(true);
    this.error.set(null);

    this.api.listMessages(conversation.id, this.messagesPage(), 20).subscribe({
      next: (page: Page<MessageResponse>) => {
        this.messages.set(page.content);
        this.messagesTotalPages.set(page.totalPages);
        this.messagesLoading.set(false);
      },
      error: (err: any) => {
        console.error('Error loading messages:', err);
        this.error.set('Unable to load messages. Please try again.');
        this.messagesLoading.set(false);
      }
    });
  }

  // Send a new message
  sendMessage() {
    const conversation = this.currentConversation();
    const messageText = this.newMessageText().trim();
    
    // Debug logging for troubleshooting
    console.log('Attempting to send message:', {
      hasConversation: !!conversation,
      conversationId: conversation?.id,
      messageText: messageText,
      messageLength: messageText.length,
      canSend: this.canSendMessage()
    });
    
    // Enhanced validation with specific error messages
    if (!conversation) {
      this.error.set('Please select a conversation first.');
      return;
    }
    
    if (!messageText) {
      this.error.set('Please enter a message before sending.');
      return;
    }
    
    if (messageText.length > 1000) {
      this.error.set('Message is too long. Maximum 1000 characters allowed.');
      return;
    }

    this.sending.set(true);
    this.error.set(null);

    this.api.sendMessage(conversation.id, { body: messageText }).subscribe({
      next: (newMessage: MessageResponse) => {
        console.log('Message sent successfully:', newMessage);
        // Add new message to the list
        this.messages.update(messages => [...messages, newMessage]);
        this.newMessageText.set('');
        this.sending.set(false);
      },
      error: (err: any) => {
        console.error('Error sending message:', err);
        let errorMessage = 'Unable to send message. Please try again.';
        
        // Enhanced error handling with specific messages
        if (err.status === 401) {
          errorMessage = 'You are not authorized. Please login again.';
        } else if (err.status === 403) {
          errorMessage = 'You do not have permission to send messages in this conversation.';
        } else if (err.status === 404) {
          errorMessage = 'Conversation not found. Please refresh the page.';
        } else if (err.status === 0) {
          errorMessage = 'Unable to connect to server. Please check your internet connection.';
        }
        
        this.error.set(errorMessage);
        this.sending.set(false);
      }
    });
  }

  // Pagination for conversations
  nextConversationsPage() {
    if (this.conversationsPage() + 1 < this.conversationsTotalPages()) {
      this.conversationsPage.update(page => page + 1);
      this.loadConversations();
    }
  }

  prevConversationsPage() {
    if (this.conversationsPage() > 0) {
      this.conversationsPage.update(page => page - 1);
      this.loadConversations();
    }
  }

  // Pagination for messages
  nextMessagesPage() {
    if (this.messagesPage() + 1 < this.messagesTotalPages()) {
      this.messagesPage.update(page => page + 1);
      this.loadMessages();
    }
  }

  prevMessagesPage() {
    if (this.messagesPage() > 0) {
      this.messagesPage.update(page => page - 1);
      this.loadMessages();
    }
  }

  // Handle Enter key in message input
  onMessageKeyDown(event: KeyboardEvent) {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      if (this.canSendMessage()) {
        this.sendMessage();
      }
    }
  }

  // Utility methods
  formatMessageTime(dateStr: string): string {
    const date = new Date(dateStr);
    return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
  }

  formatConversationDate(dateStr: string): string {
    const date = new Date(dateStr);
    return date.toLocaleDateString();
  }

  getMessageSenderLabel(role: MessageSenderRole): string {
    return role === MessageSenderRole.CLIENT ? 'Client' : 'Artisan';
  }

  isMyMessage(message: MessageResponse): boolean {
    // Determine if message is from current user based on role
    if (this.userRole === 'CLIENT') {
      return message.senderRole === MessageSenderRole.CLIENT;
    } else {
      return message.senderRole === MessageSenderRole.ARTISAN;
    }
  }

  // TrackBy functions for performance
  trackConversationById(index: number, conversation: ConversationResponse): number {
    return conversation.id;
  }

  trackMessageById(index: number, message: MessageResponse): number {
    return message.id;
  }

  // Debug method to test connection
  testConnection() {
    console.log('Testing messaging API connection...');
    this.api.myConversations(0, 1).subscribe({
      next: (response) => {
        console.log('✅ API connection successful:', response);
        this.error.set(null);
      },
      error: (err: any) => {
        console.error('❌ API connection failed:', err);
        this.error.set('Connection test failed. Check console for details.');
      }
    });
  }
}