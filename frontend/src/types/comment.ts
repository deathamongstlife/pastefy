export type Comment = {
  id: string
  paste_id: string
  user_id: string
  parent_comment_id?: string
  content: string
  line_number?: number
  created_at: string
  updated_at: string
  user?: {
    id: string
    name: string
    username: string
  }
  replies?: Comment[]
}

export type CreateCommentRequest = {
  content: string
  parentCommentId?: string
  lineNumber?: number
}
