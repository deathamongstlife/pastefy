import { eventBus } from '@/utils/event-bus'

export type CursorPosition = {
  line: number
  column: number
  selection?: {
    start: { line: number; column: number }
    end: { line: number; column: number }
  }
}

export type TextOperation = {
  type: 'insert' | 'delete' | 'replace'
  position: number
  text: string
  version: number
  userId?: string
}

export type UserInfo = {
  id: string
  username: string
  name: string
  connectionId: string
  cursor?: CursorPosition
}

export type WebSocketMessageType =
  | 'join'
  | 'cursor'
  | 'edit'
  | 'ping'
  | 'pong'
  | 'user_joined'
  | 'user_left'
  | 'cursor_update'
  | 'edit_broadcast'
  | 'error'

export type WebSocketMessage = {
  type: WebSocketMessageType
  sessionId?: string
  userId?: string
  pasteId?: string
  position?: CursorPosition
  operation?: TextOperation
  user?: UserInfo
  users?: UserInfo[]
  error?: string
}

export class CollaborationWebSocket {
  private ws: WebSocket | null = null
  private sessionId: string
  private userId: string
  private pasteId: string
  private reconnectAttempts = 0
  private maxReconnectAttempts = 5
  private heartbeatInterval: number | null = null
  private isIntentionalClose = false
  private connectionPromise: Promise<void> | null = null

  constructor(sessionId: string, userId: string, pasteId: string) {
    this.sessionId = sessionId
    this.userId = userId
    this.pasteId = pasteId
  }

  connect(): Promise<void> {
    if (this.connectionPromise) {
      return this.connectionPromise
    }

    this.connectionPromise = new Promise((resolve, reject) => {
      const wsUrl = this.getWebSocketUrl()
      console.log('Connecting to WebSocket:', wsUrl)

      try {
        this.ws = new WebSocket(wsUrl)

        this.ws.onopen = () => {
          console.log('WebSocket connected')
          this.reconnectAttempts = 0
          this.sendJoin()
          this.startHeartbeat()
          resolve()
        }

        this.ws.onmessage = (event) => {
          try {
            const message: WebSocketMessage = JSON.parse(event.data)
            this.handleMessage(message)
          } catch (error) {
            console.error('Error parsing WebSocket message:', error)
          }
        }

        this.ws.onclose = (event) => {
          console.log('WebSocket closed:', event.code, event.reason)
          this.stopHeartbeat()
          this.connectionPromise = null

          if (!this.isIntentionalClose) {
            this.handleDisconnect()
          }
        }

        this.ws.onerror = (error) => {
          console.error('WebSocket error:', error)
          reject(error)
        }
      } catch (error) {
        console.error('Error creating WebSocket:', error)
        this.connectionPromise = null
        reject(error)
      }
    })

    return this.connectionPromise
  }

  private getWebSocketUrl(): string {
    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
    const host = window.location.host
    return `${protocol}//${host}/api/v2/ws/collaboration/${this.sessionId}`
  }

  private sendJoin() {
    this.send({
      type: 'join',
      sessionId: this.sessionId,
      userId: this.userId,
      pasteId: this.pasteId
    })
  }

  sendCursorPosition(position: CursorPosition) {
    this.send({
      type: 'cursor',
      position
    })
  }

  sendEdit(operation: TextOperation) {
    this.send({
      type: 'edit',
      operation
    })
  }

  private send(message: WebSocketMessage) {
    if (this.ws?.readyState === WebSocket.OPEN) {
      try {
        this.ws.send(JSON.stringify(message))
      } catch (error) {
        console.error('Error sending WebSocket message:', error)
      }
    } else {
      console.warn('WebSocket not open, cannot send message')
    }
  }

  private handleMessage(message: WebSocketMessage) {
    eventBus.emit(`ws:${message.type}`, message)

    if (message.type === 'error') {
      console.error('WebSocket error from server:', message.error)
    }
  }

  private startHeartbeat() {
    this.heartbeatInterval = window.setInterval(() => {
      this.send({ type: 'ping' })
    }, 30000) // 30 seconds
  }

  private stopHeartbeat() {
    if (this.heartbeatInterval !== null) {
      clearInterval(this.heartbeatInterval)
      this.heartbeatInterval = null
    }
  }

  private handleDisconnect() {
    if (this.reconnectAttempts < this.maxReconnectAttempts) {
      const delay = Math.min(1000 * Math.pow(2, this.reconnectAttempts), 30000)
      console.log(`Reconnecting in ${delay}ms (attempt ${this.reconnectAttempts + 1}/${this.maxReconnectAttempts})`)

      setTimeout(() => {
        this.reconnectAttempts++
        this.connect().catch(error => {
          console.error('Reconnection failed:', error)
        })
      }, delay)
    } else {
      console.error('Max reconnection attempts reached')
      eventBus.emit('ws:connection_failed', {})
    }
  }

  disconnect() {
    this.isIntentionalClose = true
    this.stopHeartbeat()

    if (this.ws) {
      this.ws.close()
      this.ws = null
    }

    this.connectionPromise = null
  }

  isConnected(): boolean {
    return this.ws !== null && this.ws.readyState === WebSocket.OPEN
  }
}
