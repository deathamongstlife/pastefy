export type ViewData = {
  ip_address: string
  user_agent: string
  referer?: string
  country?: string
  city?: string
  viewed_at: string
}

export type DateViewData = {
  date: string
  view_count: number
  unique_view_count: number
}

export type PasteAnalytics = {
  paste_id: string
  total_views: number
  unique_views: number
  recent_views: ViewData[]
  views_by_date: DateViewData[]
  top_countries: Record<string, number>
  top_referers: Record<string, number>
}
