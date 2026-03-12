export type UserProfile = {
  id: string
  name: string
  username: string
  bio?: string
  website?: string
  location?: string
  company?: string
  avatar_url?: string
  github_username?: string
  twitter_username?: string
  paste_count?: number
  follower_count?: number
  following_count?: number
}

export type Activity = {
  id: string
  user_id: string
  activity_type: string
  target_id: string
  target_type: string
  metadata?: string
  created_at: string
  user?: {
    id: string
    name: string
    username: string
  }
}

export type UpdateProfileRequest = {
  bio?: string
  website?: string
  location?: string
  company?: string
  githubUsername?: string
  twitterUsername?: string
}

export type UserStats = {
  user_id: string
  paste_count: number
  follower_count: number
  following_count: number
}
