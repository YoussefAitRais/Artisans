import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

const API_BASE = 'http://localhost:8091';

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number; // zero-based
}

export enum MessageSenderRole {
  CLIENT = 'CLIENT',
  ARTISAN = 'ARTISAN'
}

export interface ConversationResponse {
  id: number;
  engagementId: number;
  clientId: number;
  artisanId: number;
  createdAt: string; // ISO instant
  updatedAt: string; // ISO instant
}

export interface MessageResponse {
  id: number;
  conversationId: number;
  senderRole: MessageSenderRole;
  body: string;
  createdAt: string; // ISO instant
}

export interface MessageCreateRequest {
  body: string;
}

@Injectable({ providedIn: 'root' })
export class MessagingApiService {
  private base = `${API_BASE}/api/conversations`;
  
  constructor(private http: HttpClient) {}

  // List my conversations (client or artisan)
  myConversations(page?: number, size?: number): Observable<Page<ConversationResponse>> {
    let params = new HttpParams();
    if (page != null) params = params.set('page', page);
    if (size != null) params = params.set('size', size);
    return this.http.get<Page<ConversationResponse>>(`${this.base}/my`, { params });
  }

  // Get or create conversation for an engagement
  getOrCreateByEngagement(engagementId: number): Observable<ConversationResponse> {
    return this.http.post<ConversationResponse>(`${this.base}/by-engagement/${engagementId}`, {});
  }

  // Get conversation details
  getConversation(id: number): Observable<ConversationResponse> {
    return this.http.get<ConversationResponse>(`${this.base}/${id}`);
  }

  // List messages in a conversation
  listMessages(conversationId: number, page?: number, size?: number): Observable<Page<MessageResponse>> {
    let params = new HttpParams();
    if (page != null) params = params.set('page', page);
    if (size != null) params = params.set('size', size);
    return this.http.get<Page<MessageResponse>>(`${this.base}/${conversationId}/messages`, { params });
  }

  // Send a message in a conversation
  sendMessage(conversationId: number, request: MessageCreateRequest): Observable<MessageResponse> {
    return this.http.post<MessageResponse>(`${this.base}/${conversationId}/messages`, request);
  }
}