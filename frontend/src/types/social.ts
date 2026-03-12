/**
 * Type definitions for Social Features
 */

export interface UserFollow {
  id: string
  followerId: string
  followingId: string
  createdAt: string
}

export interface PasteComment {
  id: string
  pasteId: string
  userId: string
  parentCommentId?: string
  content: string
  isEdited: boolean
  createdAt: string
  updatedAt: string
  isDeleted: boolean
  user?: {
    id: string
    name: string
    avatarUrl?: string
  }
  replies?: PasteComment[]
}

export interface UserActivity {
  id: string
  userId: string
  activityType: string
  targetId: string
  metadata?: string
  createdAt: string
}

export interface UserProfile {
  userId: string
  bio?: string
  website?: string
  twitter?: string
  github?: string
  linkedin?: string
  location?: string
  company?: string
  avatarUrl?: string
  emailNotifications: boolean
  activityFeedPublic: boolean
  theme: string
  editorTheme: string
  fontSize: number
}

export interface CommentCreateRequest {
  content: string
  parentCommentId?: string
}

export interface CommentUpdateRequest {
  content: string
}

export interface UserProfileUpdateRequest {
  bio?: string
  website?: string
  twitter?: string
  github?: string
  linkedin?: string
  location?: string
  company?: string
  avatarUrl?: string
  emailNotifications?: boolean
  activityFeedPublic?: boolean
  theme?: string
  editorTheme?: string
  fontSize?: number
}
