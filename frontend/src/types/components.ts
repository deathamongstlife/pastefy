export type Language = {
  label: string
  value: string
}

export type BugType = 'BUG' | 'SECURITY' | 'SMELL' | 'PERFORMANCE'
export type Severity = 'HIGH' | 'MEDIUM' | 'LOW'
export type ConnectionStatus = 'connected' | 'disconnected' | 'connecting' | 'error'
export type ViewMode = 'split' | 'diff' | 'current' | 'parent'

export type Bug = {
  id: string
  type: BugType
  severity: Severity
  line: number
  description: string
  details: string
  suggestion: string
}

export type Improvement = {
  id: string
  title: string
  description: string
  original: string
  improved: string
  accepted: boolean
}

export type QualityMetric = {
  name: string
  score: number
  description: string
}

export type Suggestion = {
  id: string
  title: string
  description: string
  impact: 'HIGH' | 'MEDIUM' | 'LOW'
  category: string
}

export type QualityAnalysis = {
  overall_score: number
  readability: number
  maintainability: number
  complexity: number
  metrics: QualityMetric[]
  suggestions: Suggestion[]
}

export type Revision = {
  id: string
  commit_message: string
  author: {
    id: string
    username: string
    avatar_url?: string
  }
  branch: string
  created_at: string
  changes_count: number
  parent_revision_id?: string
}

export type DiffLine = {
  line_number: number
  type: 'added' | 'removed' | 'unchanged'
  content: string
}

export type Branch = {
  name: string
  is_default: boolean
  head_revision_id: string
  created_at: string
  updated_at: string
}

export type CollaborationUser = {
  id: string
  username: string
  avatar_url?: string
  cursor_position: number
  cursor_color: string
  status: 'online' | 'away' | 'offline'
  last_seen: string
}

export type ChatMessage = {
  id: string
  user: {
    id: string
    username: string
    avatar_url?: string
  }
  message: string
  created_at: string
  mentions: string[]
}

export type AccessLog = {
  id: string
  timestamp: string
  ip_address: string
  user?: {
    username: string
  }
  action: string
  granted: boolean
  deny_reason?: string
}

export type UserNotification = {
  id: string
  type: 'COMMENT' | 'FOLLOW' | 'STAR' | 'MENTION' | 'PASTE' | 'SYSTEM'
  title: string
  message: string
  read: boolean
  created_at: string
  action_url?: string
  actor?: {
    username: string
    avatar_url?: string
  }
}

export type Comment = {
  id: string
  user: {
    id: string
    username: string
    avatar_url?: string
  }
  content: string
  created_at: string
  updated_at?: string
  likes: number
  liked: boolean
  replies: Comment[]
  parent_id?: string
}

export type Activity = {
  id: string
  type: 'paste_created' | 'comment' | 'star' | 'follow'
  user: {
    id: string
    username: string
    avatar_url?: string
  }
  paste?: {
    id: string
    title: string
  }
  created_at: string
  description: string
}

export type Collection = {
  id: string
  name: string
  description: string
  cover_image_url?: string
  paste_count: number
  visibility: 'PUBLIC' | 'PRIVATE'
  creator: {
    id: string
    username: string
  }
  created_at: string
  starred: boolean
}

export type AnalyticsData = {
  total_views: number
  unique_views: number
  views_today: number
  trend_percentage: number
  chart_data: {
    date: string
    views: number
  }[]
  geographic_data: {
    country: string
    views: number
  }[]
  top_referrers: {
    source: string
    views: number
  }[]
}

export type Webhook = {
  id: string
  url: string
  events: string[]
  secret: string
  enabled: boolean
  created_at: string
  last_triggered?: string
}

export type WebhookLog = {
  id: string
  webhook_id: string
  status_code: number
  response_time_ms: number
  created_at: string
  payload: Record<string, unknown>
  response: string
  success: boolean
}

export type Template = {
  id: string
  name: string
  description: string
  code: string
  language: string
  category: string
  tags: string[]
  author: {
    username: string
  }
  use_count: number
  created_at: string
}
