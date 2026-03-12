/**
 * Type definitions for Real-time Collaboration
 */

export interface CollaborationSession {
  id: string
  pasteId: string
  ownerId: string
  sessionToken: string
  isActive: boolean
  maxParticipants: number
  createdAt: string
  expiresAt?: string
  lastActivityAt: string
}

export interface CollaborationCursor {
  id: string
  sessionId: string
  userId: string
  userDisplayName: string
  userColor: string
  cursorLine: number
  cursorColumn: number
  selectionStartLine?: number
  selectionStartColumn?: number
  selectionEndLine?: number
  selectionEndColumn?: number
  lastSeenAt: string
}

export interface CollaborationSessionCreateRequest {
  pasteKey: string
  maxParticipants?: number
  expiresInHours?: number
}

export interface CursorUpdateRequest {
  userId: string
  userDisplayName: string
  userColor: string
  cursorLine: number
  cursorColumn: number
  selectionStartLine?: number
  selectionStartColumn?: number
  selectionEndLine?: number
  selectionEndColumn?: number
}

export interface CollaborationMessage {
  type: 'cursor' | 'edit' | 'join' | 'leave'
  sessionId: string
  userId: string
  data: any
  timestamp: string
}
