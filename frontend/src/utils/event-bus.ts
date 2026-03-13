import mitt, { type Emitter } from 'mitt'

export type Events = {
  'ws:join': any
  'ws:cursor': any
  'ws:edit': any
  'ws:ping': any
  'ws:pong': any
  'ws:user_joined': any
  'ws:user_left': any
  'ws:cursor_update': any
  'ws:edit_broadcast': any
  'ws:error': any
  'ws:connection_failed': any
  'collaboration:edit': any
}

export const eventBus: Emitter<Events> = mitt<Events>()
