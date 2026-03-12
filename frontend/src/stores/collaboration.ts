import { ref } from 'vue'
import { defineStore } from 'pinia'
import type {
  CollaborationSession,
  CollaborationCursor,
  CollaborationSessionCreateRequest,
  CursorUpdateRequest
} from '@/types/collaboration'
import { client } from '@/main'

export const useCollaborationStore = defineStore('collaboration', () => {
  const session = ref<CollaborationSession | undefined>(undefined)
  const cursors = ref<CollaborationCursor[]>([])
  const loading = ref(false)
  const connected = ref(false)
  let ws: WebSocket | null = null

  async function createSession(request: CollaborationSessionCreateRequest) {
    loading.value = true
    try {
      const response = await client.post('/api/v2/collaboration/sessions', request)
      session.value = response.data
      return response.data
    } catch (error) {
      console.error('Failed to create collaboration session:', error)
      return null
    } finally {
      loading.value = false
    }
  }

  async function joinSession(sessionToken: string) {
    loading.value = true
    try {
      const response = await client.get(`/api/v2/collaboration/sessions/token/${sessionToken}`)
      session.value = response.data
      await fetchCursors()
      return response.data
    } catch (error) {
      console.error('Failed to join collaboration session:', error)
      return null
    } finally {
      loading.value = false
    }
  }

  async function fetchCursors() {
    if (!session.value) return

    try {
      const response = await client.get(`/api/v2/collaboration/sessions/${session.value.id}/cursors`)
      cursors.value = response.data
    } catch (error) {
      console.error('Failed to fetch cursors:', error)
    }
  }

  async function updateCursor(request: CursorUpdateRequest) {
    if (!session.value) return

    try {
      await client.post(`/api/v2/collaboration/sessions/${session.value.id}/cursors`, request)
    } catch (error) {
      console.error('Failed to update cursor:', error)
    }
  }

  async function closeSession() {
    if (!session.value) return

    loading.value = true
    try {
      await client.delete(`/api/v2/collaboration/sessions/${session.value.id}`)
      disconnect()
      session.value = undefined
      cursors.value = []
    } catch (error) {
      console.error('Failed to close session:', error)
    } finally {
      loading.value = false
    }
  }

  function connectWebSocket(sessionId: string) {
    // Note: WebSocket implementation would be added based on server configuration
    // This is a placeholder for the WebSocket connection logic
    disconnect()

    const wsProtocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
    const wsUrl = `${wsProtocol}//${window.location.host}/ws/collaboration/${sessionId}`

    ws = new WebSocket(wsUrl)

    ws.onopen = () => {
      connected.value = true
      console.log('WebSocket connected')
    }

    ws.onmessage = (event) => {
      const data = JSON.parse(event.data)
      handleWebSocketMessage(data)
    }

    ws.onerror = (error) => {
      console.error('WebSocket error:', error)
      connected.value = false
    }

    ws.onclose = () => {
      connected.value = false
      console.log('WebSocket disconnected')
    }
  }

  function disconnect() {
    if (ws) {
      ws.close()
      ws = null
    }
    connected.value = false
  }

  function handleWebSocketMessage(data: any) {
    if (data.type === 'cursor') {
      const cursorIndex = cursors.value.findIndex((c) => c.userId === data.userId)
      if (cursorIndex >= 0) {
        cursors.value[cursorIndex] = data.cursor
      } else {
        cursors.value.push(data.cursor)
      }
    } else if (data.type === 'leave') {
      cursors.value = cursors.value.filter((c) => c.userId !== data.userId)
    }
  }

  function reset() {
    disconnect()
    session.value = undefined
    cursors.value = []
  }

  return {
    session,
    cursors,
    loading,
    connected,
    createSession,
    joinSession,
    fetchCursors,
    updateCursor,
    closeSession,
    connectWebSocket,
    disconnect,
    reset
  }
})
