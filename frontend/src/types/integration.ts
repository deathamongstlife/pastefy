/**
 * Type definitions for Integrations
 */

export interface Webhook {
  id: string
  userId: string
  url: string
  secret: string
  events: string
  isActive: boolean
  failureCount: number
  lastTriggeredAt?: string
  createdAt: string
  updatedAt: string
}

export interface WebhookEvent {
  id: string
  webhookId: string
  eventType: string
  payload: string
  responseCode?: number
  responseBody?: string
  success: boolean
  attemptedAt: string
}

export interface WebhookCreateRequest {
  url: string
  events: string
}

export interface WebhookUpdateRequest {
  url?: string
  events?: string
  isActive?: boolean
}

export interface PasteAttachment {
  id: string
  pasteId: string
  filename: string
  originalFilename: string
  mimeType: string
  fileSize: number
  storageType: string
  storagePath: string
  thumbnailPath?: string
  createdAt: string
}

export interface CodeTemplate {
  id: string
  userId: string
  name: string
  description?: string
  language: string
  content: string
  category?: string
  isPublic: boolean
  useCount: number
  createdAt: string
  updatedAt: string
}

export interface TemplateCreateRequest {
  name: string
  description?: string
  language: string
  content: string
  category?: string
  isPublic?: boolean
}

export interface TemplateUpdateRequest {
  name?: string
  description?: string
  content?: string
  category?: string
  isPublic?: boolean
}
