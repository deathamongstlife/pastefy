import { ref } from 'vue'
import { defineStore } from 'pinia'
import type {
  ViewAnalytics,
  TimelineDataPoint,
  GeographicDataPoint,
  TrendingItem,
  ViewTrackRequest
} from '@/types/analytics'
import { client } from '@/main'

export const useAnalyticsStore = defineStore('analytics', () => {
  const analytics = ref<ViewAnalytics | undefined>(undefined)
  const timeline = ref<TimelineDataPoint[]>([])
  const geographic = ref<GeographicDataPoint[]>([])
  const trending = ref<TrendingItem[]>([])
  const loading = ref(false)

  async function trackView(pasteKey: string, request: ViewTrackRequest = {}) {
    try {
      await client.post(`/api/v2/analytics/paste/${pasteKey}/view`, request)
    } catch (error) {
      console.error('Failed to track view:', error)
    }
  }

  async function fetchAnalytics(pasteKey: string) {
    loading.value = true
    try {
      const response = await client.get(`/api/v2/analytics/paste/${pasteKey}`)
      analytics.value = response.data
    } catch (error) {
      console.error('Failed to fetch analytics:', error)
      analytics.value = undefined
    }
    loading.value = false
  }

  async function fetchTimeline(pasteKey: string, days: number = 30) {
    loading.value = true
    try {
      const response = await client.get(`/api/v2/analytics/paste/${pasteKey}/views/timeline`, {
        params: { days }
      })
      timeline.value = response.data
    } catch (error) {
      console.error('Failed to fetch timeline:', error)
      timeline.value = []
    }
    loading.value = false
  }

  async function fetchGeographic(pasteKey: string) {
    loading.value = true
    try {
      const response = await client.get(`/api/v2/analytics/paste/${pasteKey}/views/geographic`)
      geographic.value = response.data
    } catch (error) {
      console.error('Failed to fetch geographic data:', error)
      geographic.value = []
    }
    loading.value = false
  }

  async function fetchTrending(limit: number = 20) {
    loading.value = true
    try {
      const response = await client.get('/api/v2/analytics/trending', {
        params: { limit }
      })
      trending.value = response.data
    } catch (error) {
      console.error('Failed to fetch trending:', error)
      trending.value = []
    }
    loading.value = false
  }

  function reset() {
    analytics.value = undefined
    timeline.value = []
    geographic.value = []
  }

  return {
    analytics,
    timeline,
    geographic,
    trending,
    loading,
    trackView,
    fetchAnalytics,
    fetchTimeline,
    fetchGeographic,
    fetchTrending,
    reset
  }
})
