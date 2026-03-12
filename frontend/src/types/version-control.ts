export type PasteRevision = {
  id: string
  paste_id: string
  branch_id: string
  parent_revision_id: string | null
  revision_number: number
  commit_message: string
  author: PublicUser
  created_at: string
  diff?: string
}

export type PasteBranch = {
  id: string
  paste_id: string
  name: string
  head_revision_id: string
  is_default: boolean
  creator: PublicUser
  created_at: string
  updated_at: string
}

export type PublicUser = {
  id: string
  name: string
  username: string
}

export type CreateBranchRequest = {
  name: string
  fromRevisionId?: string
}

export type CommitRequest = {
  message: string
  branch?: string
}

export type CompareResponse = {
  fromRevision: PasteRevision
  toRevision: PasteRevision
  diff: string
}

export type RevisionContent = {
  content: string
}

// Git-like diff stats
export type DiffStats = {
  additions: number
  deletions: number
  changes: number
}

// Parsed diff line for rendering
export type DiffLine = {
  type: 'add' | 'remove' | 'context' | 'header'
  content: string
  oldLineNumber?: number
  newLineNumber?: number
}

// Real-time Collaboration Types
export type CollaborationSession = {
  id: string
  paste_id: string
  owner_id: string
  session_token: string
  is_active: boolean
  max_participants: number
  created_at: string
  expires_at: string | null
  last_activity_at: string
}

export type CollaborationUser = {
  userId: string
  userName: string
  color: string
}

export type CursorPosition = {
  userId: string
  userName: string
  line: number
  column: number
  color: string
}

export type EditOperation = {
  userId: string
  userName: string
  position: number
  text: string
  type: 'insert' | 'delete'
  length: number
  timestamp: number
}

export type WebSocketMessage = {
  type: 'init' | 'user_joined' | 'user_left' | 'edit' | 'cursor' | 'selection' | 'typing' | 'error'
  data: any
}

export type InitialState = {
  content: string
  activeUsers: CollaborationUser[]
  cursors: CursorPosition[]
}

// Advanced Security Types
export type PasteAccess = {
  id: string
  paste_id: string
  password_hash: string | null
  ip_whitelist: string | null
  ip_blacklist: string | null
  expires_at: string | null
  max_views: number | null
  current_views: number
  requires_auth: boolean
  created_at: string
  updated_at: string
}

export type AccessLog = {
  id: string
  paste_id: string
  user_id: string | null
  ip_address: string
  user_agent: string
  access_type: string
  access_granted: boolean
  deny_reason: string | null
  accessed_at: string
  referer: string | null
  country: string | null
  city: string | null
}

export type SetAccessRequest = {
  password?: string
  ipWhitelist?: string
  ipBlacklist?: string
  maxViews?: number
  expiresAt?: string
  requiresAuth?: boolean
}

export type VerifyAccessRequest = {
  password?: string
}
