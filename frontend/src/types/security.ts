/**
 * Type definitions for Advanced Security features
 */

export interface PasteAccess {
  id: string
  pasteId: string
  passwordHash?: string
  ipWhitelist?: string
  ipBlacklist?: string
  expiresAt?: string
  maxViews?: number
  currentViews: number
  requiresAuth: boolean
  createdAt: string
  updatedAt: string
}

export interface AccessLog {
  id: string
  pasteId: string
  userId?: string
  ipAddress: string
  userAgent?: string
  accessType: string
  accessGranted: boolean
  denyReason?: string
  accessedAt: string
  referer?: string
  country?: string
  city?: string
}

export interface PasteAccessRequest {
  password?: string
  ipWhitelist?: string
  ipBlacklist?: string
  maxViews?: number
  expiresAt?: string
  requiresAuth?: boolean
}

export interface AccessVerifyRequest {
  password?: string
}

export interface AccessVerifyResponse {
  success: boolean
  message?: string
}
