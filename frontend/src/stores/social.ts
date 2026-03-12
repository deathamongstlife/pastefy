import { ref } from 'vue'
import { defineStore } from 'pinia'
import type {
  UserFollow,
  PasteComment,
  UserActivity,
  UserProfile,
  CommentCreateRequest,
  CommentUpdateRequest,
  UserProfileUpdateRequest
} from '@/types/social'
import { client } from '@/main'

export const useSocialStore = defineStore('social', () => {
  const followers = ref<UserFollow[]>([])
  const following = ref<UserFollow[]>([])
  const comments = ref<PasteComment[]>([])
  const activities = ref<UserActivity[]>([])
  const activityFeed = ref<UserActivity[]>([])
  const profile = ref<UserProfile | undefined>(undefined)
  const loading = ref(false)

  async function followUser(userId: string) {
    loading.value = true
    try {
      await client.post(`/api/v2/social/follow/${userId}`)
      return true
    } catch (error) {
      console.error('Failed to follow user:', error)
      return false
    } finally {
      loading.value = false
    }
  }

  async function unfollowUser(userId: string) {
    loading.value = true
    try {
      await client.delete(`/api/v2/social/follow/${userId}`)
      return true
    } catch (error) {
      console.error('Failed to unfollow user:', error)
      return false
    } finally {
      loading.value = false
    }
  }

  async function fetchFollowers(userId: string, limit: number = 50) {
    loading.value = true
    try {
      const response = await client.get(`/api/v2/social/followers/${userId}`, {
        params: { limit }
      })
      followers.value = response.data
    } catch (error) {
      console.error('Failed to fetch followers:', error)
      followers.value = []
    }
    loading.value = false
  }

  async function fetchFollowing(userId: string, limit: number = 50) {
    loading.value = true
    try {
      const response = await client.get(`/api/v2/social/following/${userId}`, {
        params: { limit }
      })
      following.value = response.data
    } catch (error) {
      console.error('Failed to fetch following:', error)
      following.value = []
    }
    loading.value = false
  }

  async function fetchComments(pasteKey: string, limit: number = 50) {
    loading.value = true
    try {
      const response = await client.get(`/api/v2/social/paste/${pasteKey}/comments`, {
        params: { limit }
      })
      comments.value = response.data
    } catch (error) {
      console.error('Failed to fetch comments:', error)
      comments.value = []
    }
    loading.value = false
  }

  async function addComment(pasteKey: string, request: CommentCreateRequest) {
    loading.value = true
    try {
      const response = await client.post(`/api/v2/social/paste/${pasteKey}/comments`, request)
      comments.value.unshift(response.data)
      return response.data
    } catch (error) {
      console.error('Failed to add comment:', error)
      return null
    } finally {
      loading.value = false
    }
  }

  async function updateComment(commentId: string, request: CommentUpdateRequest) {
    loading.value = true
    try {
      const response = await client.put(`/api/v2/social/comments/${commentId}`, request)
      const index = comments.value.findIndex((c) => c.id === commentId)
      if (index >= 0) {
        comments.value[index] = response.data
      }
      return response.data
    } catch (error) {
      console.error('Failed to update comment:', error)
      return null
    } finally {
      loading.value = false
    }
  }

  async function deleteComment(commentId: string) {
    loading.value = true
    try {
      await client.delete(`/api/v2/social/comments/${commentId}`)
      comments.value = comments.value.filter((c) => c.id !== commentId)
      return true
    } catch (error) {
      console.error('Failed to delete comment:', error)
      return false
    } finally {
      loading.value = false
    }
  }

  async function fetchActivities(userId: string, limit: number = 50) {
    loading.value = true
    try {
      const response = await client.get(`/api/v2/social/activity/${userId}`, {
        params: { limit }
      })
      activities.value = response.data
    } catch (error) {
      console.error('Failed to fetch activities:', error)
      activities.value = []
    }
    loading.value = false
  }

  async function fetchActivityFeed(limit: number = 50) {
    loading.value = true
    try {
      const response = await client.get('/api/v2/social/feed', {
        params: { limit }
      })
      activityFeed.value = response.data
    } catch (error) {
      console.error('Failed to fetch activity feed:', error)
      activityFeed.value = []
    }
    loading.value = false
  }

  function reset() {
    followers.value = []
    following.value = []
    comments.value = []
    activities.value = []
    activityFeed.value = []
    profile.value = undefined
  }

  return {
    followers,
    following,
    comments,
    activities,
    activityFeed,
    profile,
    loading,
    followUser,
    unfollowUser,
    fetchFollowers,
    fetchFollowing,
    fetchComments,
    addComment,
    updateComment,
    deleteComment,
    fetchActivities,
    fetchActivityFeed,
    reset
  }
})
