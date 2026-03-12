/**
 * Type definitions for Analytics & Tracking
 */

export interface PasteView {
  id: string
  pasteId: string
  userId?: string
  ipAddress: string
  userAgent?: string
  referer?: string
  country?: string
  city?: string
  viewedAt: string
  timeSpent?: number
}

export interface ViewAnalytics {
  id: string
  pasteId: string
  totalViews: number
  uniqueViews: number
  todayViews: number
  weekViews: number
  monthViews: number
  averageTimeSpent: number
  trendingScore: number
  lastViewedAt?: string
  lastCalculatedAt: string
}

export interface ViewTrackRequest {
  timeSpent?: number
}

export interface TimelineDataPoint {
  date: string
  views: number
}

export interface GeographicDataPoint {
  country: string
  views: number
}

export interface TrendingItem {
  paste: any
  analytics: ViewAnalytics
}
