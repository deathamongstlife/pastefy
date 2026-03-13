import { ref, onUnmounted } from 'vue'
import { CollaborationWebSocket, type CursorPosition, type TextOperation, type UserInfo } from '@/services/collaborationWebSocket'
import { eventBus } from '@/utils/event-bus'
import { useCurrentUserStore } from '@/stores/current-user'

export function useCollaboration(sessionId: string, pasteId: string) {
  const ws = ref<CollaborationWebSocket | null>(null)
  const users = ref<UserInfo[]>([])
  const cursors = ref<Map<string, CursorPosition>>(new Map())
  const isConnected = ref(false)
  const connectionError = ref<string | null>(null)
  const currentUserStore = useCurrentUserStore()

  const handleUserJoined = (message: any) => {
    if (message.users) {
      users.value = message.users
    }
    if (message.user) {
      console.log('User joined:', message.user.username)
    }
  }

  const handleUserLeft = (message: any) => {
    if (message.userId) {
      users.value = users.value.filter(u => u.id !== message.userId)
      cursors.value.delete(message.userId)
      console.log('User left:', message.userId)
    }
  }

  const handleCursorUpdate = (message: any) => {
    if (message.userId && message.position) {
      cursors.value.set(message.userId, message.position)
    }
  }

  const handleEditBroadcast = (message: any) => {
    if (message.operation) {
      eventBus.emit('collaboration:edit', message.operation)
    }
  }

  const handleError = (message: any) => {
    if (message.error) {
      connectionError.value = message.error
      console.error('Collaboration error:', message.error)
    }
  }

  const handleConnectionFailed = () => {
    isConnected.value = false
    connectionError.value = 'Connection failed. Please refresh the page.'
  }

  const connect = async () => {
    if (!currentUserStore.user?.id) {
      connectionError.value = 'User not logged in'
      return
    }

    try {
      ws.value = new CollaborationWebSocket(
        sessionId,
        currentUserStore.user.id,
        pasteId
      )

      // Set up event listeners
      eventBus.on('ws:user_joined', handleUserJoined)
      eventBus.on('ws:user_left', handleUserLeft)
      eventBus.on('ws:cursor_update', handleCursorUpdate)
      eventBus.on('ws:edit_broadcast', handleEditBroadcast)
      eventBus.on('ws:error', handleError)
      eventBus.on('ws:connection_failed', handleConnectionFailed)

      await ws.value.connect()
      isConnected.value = true
      connectionError.value = null

      console.log('Collaboration session connected:', sessionId)
    } catch (error) {
      console.error('Failed to connect to collaboration session:', error)
      connectionError.value = 'Failed to connect to collaboration session'
      isConnected.value = false
    }
  }

  const disconnect = () => {
    if (ws.value) {
      ws.value.disconnect()
      ws.value = null
    }

    isConnected.value = false
    users.value = []
    cursors.value.clear()

    // Clean up event listeners
    eventBus.off('ws:user_joined', handleUserJoined)
    eventBus.off('ws:user_left', handleUserLeft)
    eventBus.off('ws:cursor_update', handleCursorUpdate)
    eventBus.off('ws:edit_broadcast', handleEditBroadcast)
    eventBus.off('ws:error', handleError)
    eventBus.off('ws:connection_failed', handleConnectionFailed)

    console.log('Collaboration session disconnected')
  }

  const sendCursorPosition = (position: CursorPosition) => {
    ws.value?.sendCursorPosition(position)
  }

  const sendEdit = (operation: TextOperation) => {
    ws.value?.sendEdit(operation)
  }

  const getCurrentUser = () => {
    return users.value.find(u => u.id === currentUserStore.user?.id)
  }

  const getOtherUsers = () => {
    return users.value.filter(u => u.id !== currentUserStore.user?.id)
  }

  onUnmounted(() => {
    disconnect()
  })

  return {
    users,
    cursors,
    isConnected,
    connectionError,
    connect,
    disconnect,
    sendCursorPosition,
    sendEdit,
    getCurrentUser,
    getOtherUsers
  }
}
