<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { client } from '@/utils/client'
import type { CollaborationUser } from '@/types/components'
import Card from 'primevue/card'
import Avatar from 'primevue/avatar'
import Button from 'primevue/button'
import InputText from 'primevue/inputtext'
import Dialog from 'primevue/dialog'
import Tag from 'primevue/tag'

const props = defineProps<{
  pasteId: string
  sessionId: string
}>()

const emit = defineEmits<{
  userJoined: [user: CollaborationUser]
  userLeft: [userId: string]
  inviteSent: [email: string]
}>()

const activeUsers = ref<CollaborationUser[]>([])
const showInviteDialog = ref(false)
const inviteEmail = ref('')
const isLoading = ref(false)
let ws: WebSocket | null = null

const onlineUsers = computed(() =>
  activeUsers.value.filter(u => u.status === 'online')
)

const awayUsers = computed(() =>
  activeUsers.value.filter(u => u.status === 'away')
)

function getUserStatusColor(status: string): string {
  switch (status) {
    case 'online':
      return '#22c55e'
    case 'away':
      return '#eab308'
    case 'offline':
      return '#6b7280'
    default:
      return '#6b7280'
  }
}

function formatLastSeen(lastSeen: string): string {
  const date = new Date(lastSeen)
  const now = new Date()
  const diffMs = now.getTime() - date.getTime()
  const diffMins = Math.floor(diffMs / 60000)

  if (diffMins < 1) return 'Just now'
  if (diffMins < 60) return `${diffMins}m ago`
  if (diffMins < 1440) return `${Math.floor(diffMins / 60)}h ago`
  return date.toLocaleDateString()
}

function connectWebSocket() {
  const wsProtocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
  const wsUrl = `${wsProtocol}//${window.location.host}/api/v2/collaboration/${props.sessionId}/ws`

  ws = new WebSocket(wsUrl)

  ws.onopen = () => {
    console.log('Collaboration WebSocket connected')
  }

  ws.onmessage = (event) => {
    const message = JSON.parse(event.data)

    switch (message.type) {
      case 'user_joined':
        activeUsers.value.push(message.user)
        emit('userJoined', message.user)
        break
      case 'user_left':
        activeUsers.value = activeUsers.value.filter(u => u.id !== message.user_id)
        emit('userLeft', message.user_id)
        break
      case 'user_list':
        activeUsers.value = message.users
        break
      case 'user_status_changed':
        const user = activeUsers.value.find(u => u.id === message.user_id)
        if (user) {
          user.status = message.status
        }
        break
    }
  }

  ws.onerror = (error) => {
    console.error('WebSocket error:', error)
  }

  ws.onclose = () => {
    console.log('WebSocket closed, attempting reconnect...')
    setTimeout(connectWebSocket, 3000)
  }
}

async function inviteUser() {
  if (!inviteEmail.value.trim()) return

  isLoading.value = true
  try {
    await client.post(`/api/v2/collaboration/${props.sessionId}/invite`, {
      email: inviteEmail.value
    })

    emit('inviteSent', inviteEmail.value)
    showInviteDialog.value = false
    inviteEmail.value = ''
  } catch (e) {
    console.error('Failed to send invite:', e)
  } finally {
    isLoading.value = false
  }
}

onMounted(() => {
  connectWebSocket()
})

onUnmounted(() => {
  if (ws) {
    ws.close()
  }
})
</script>

<template>
  <Card class="collaboration-panel">
    <template #title>
      <div class="flex items-center justify-between">
        <div class="flex items-center gap-2">
          <span>Active Users</span>
          <Tag :value="onlineUsers.length.toString()" severity="success" />
        </div>
        <Button
          icon="pi pi-user-plus"
          size="small"
          text
          @click="showInviteDialog = true"
          v-tooltip.left="'Invite user'"
        />
      </div>
    </template>

    <template #content>
      <div class="space-y-4">
        <div v-if="onlineUsers.length > 0" class="online-users">
          <h4 class="mb-2 text-sm font-semibold text-gray-600 dark:text-gray-400">
            Online ({{ onlineUsers.length }})
          </h4>
          <div class="space-y-2">
            <div
              v-for="user in onlineUsers"
              :key="user.id"
              class="flex items-center gap-3 rounded p-2 hover:bg-gray-50 dark:hover:bg-gray-800"
            >
              <div class="relative">
                <Avatar
                  :image="user.avatar_url"
                  :label="user.username[0].toUpperCase()"
                  shape="circle"
                  size="large"
                />
                <span
                  class="absolute bottom-0 right-0 h-3 w-3 rounded-full border-2 border-white dark:border-gray-900"
                  :style="{ backgroundColor: getUserStatusColor(user.status) }"
                ></span>
              </div>
              <div class="flex-1">
                <div class="font-medium">{{ user.username }}</div>
                <div class="flex items-center gap-2 text-xs text-gray-500">
                  <div
                    class="h-2 w-2 rounded-full"
                    :style="{ backgroundColor: user.cursor_color }"
                  ></div>
                  <span>Cursor position: {{ user.cursor_position }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div v-if="awayUsers.length > 0" class="away-users">
          <h4 class="mb-2 text-sm font-semibold text-gray-600 dark:text-gray-400">
            Away ({{ awayUsers.length }})
          </h4>
          <div class="space-y-2">
            <div
              v-for="user in awayUsers"
              :key="user.id"
              class="flex items-center gap-3 rounded p-2 opacity-60"
            >
              <Avatar
                :image="user.avatar_url"
                :label="user.username[0].toUpperCase()"
                shape="circle"
              />
              <div class="flex-1">
                <div class="font-medium">{{ user.username }}</div>
                <div class="text-xs text-gray-500">
                  {{ formatLastSeen(user.last_seen) }}
                </div>
              </div>
            </div>
          </div>
        </div>

        <div v-if="activeUsers.length === 0" class="text-center text-sm text-gray-500">
          No active users
        </div>
      </div>
    </template>
  </Card>

  <Dialog
    v-model:visible="showInviteDialog"
    header="Invite User to Collaborate"
    :modal="true"
    :style="{ width: '450px' }"
  >
    <div class="space-y-4">
      <div>
        <label for="invite-email" class="mb-2 block text-sm font-medium">
          Email Address
        </label>
        <InputText
          id="invite-email"
          v-model="inviteEmail"
          type="email"
          placeholder="user@example.com"
          class="w-full"
          @keyup.enter="inviteUser"
        />
      </div>
    </div>

    <template #footer>
      <Button
        label="Cancel"
        severity="secondary"
        outlined
        @click="showInviteDialog = false"
      />
      <Button
        label="Send Invite"
        icon="pi pi-send"
        :loading="isLoading"
        :disabled="!inviteEmail.trim()"
        @click="inviteUser"
      />
    </template>
  </Dialog>
</template>

<style scoped>
.collaboration-panel {
  width: 100%;
  max-width: 400px;
}

.online-users,
.away-users {
  padding-bottom: 1rem;
  border-bottom: 1px solid var(--surface-border);
}

.away-users {
  border-bottom: none;
}
</style>
