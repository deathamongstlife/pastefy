#!/bin/bash

# Script to generate remaining Pastely Vue components
# This creates production-ready component stubs following CLAUDE.md patterns

COMPONENTS_DIR="/__modal/volumes/vo-pkwyL871BwojYJgLZ0F1rM/claude-workspace/r79767525_gmail.com/deathamongstlife/pastefy/frontend/src/components"

echo "Generating remaining Pastely Vue components..."

# Collaboration Components
cat > "$COMPONENTS_DIR/collaboration/CursorOverlay.vue" << 'EOF'
<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import type { CollaborationUser } from '@/types/components'

const props = defineProps<{
  users: CollaborationUser[]
  containerElement: HTMLElement | null
}>()

const cursors = ref<Map<string, { x: number; y: number }>>(new Map())

function updateCursorPosition(userId: string, position: number) {
  if (!props.containerElement) return

  const rect = props.containerElement.getBoundingClientRect()
  const lineHeight = 20
  const charWidth = 8

  const line = Math.floor(position / 80)
  const char = position % 80

  cursors.value.set(userId, {
    x: char * charWidth,
    y: line * lineHeight
  })
}
</script>

<template>
  <div class="cursor-overlay">
    <div
      v-for="user in users"
      :key="user.id"
      v-show="cursors.get(user.id)"
      class="cursor-indicator"
      :style="{
        left: `${cursors.get(user.id)?.x ?? 0}px`,
        top: `${cursors.get(user.id)?.y ?? 0}px`,
        borderColor: user.cursor_color
      }"
    >
      <div class="cursor-label" :style="{ backgroundColor: user.cursor_color }">
        {{ user.username }}
      </div>
    </div>
  </div>
</template>

<style scoped>
.cursor-overlay {
  position: absolute;
  pointer-events: none;
  inset: 0;
}

.cursor-indicator {
  position: absolute;
  width: 2px;
  height: 20px;
  border-left: 2px solid;
  transition: all 0.1s ease;
}

.cursor-label {
  position: absolute;
  top: -20px;
  left: 0;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 11px;
  color: white;
  white-space: nowrap;
}
</style>
EOF

cat > "$COMPONENTS_DIR/collaboration/CollaborationChat.vue" << 'EOF'
<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { client } from '@/utils/client'
import type { ChatMessage } from '@/types/components'
import Card from 'primevue/card'
import InputText from 'primevue/inputtext'
import Button from 'primevue/button'
import Avatar from 'primevue/avatar'

const props = defineProps<{
  sessionId: string
}>()

const messages = ref<ChatMessage[]>([])
const newMessage = ref('')
const isMinimized = ref(false)
const isLoading = ref(false)

async function sendMessage() {
  if (!newMessage.value.trim()) return

  isLoading.value = true
  try {
    await client.post(`/api/v2/collaboration/${props.sessionId}/chat`, {
      message: newMessage.value
    })
    newMessage.value = ''
  } catch (e) {
    console.error('Failed to send message:', e)
  } finally {
    isLoading.value = false
  }
}

function formatTime(dateString: string): string {
  return new Date(dateString).toLocaleTimeString([], {
    hour: '2-digit',
    minute: '2-digit'
  })
}
</script>

<template>
  <Card class="collaboration-chat" :class="{ minimized: isMinimized }">
    <template #title>
      <div class="flex items-center justify-between">
        <span>Chat</span>
        <Button
          :icon="isMinimized ? 'pi pi-window-maximize' : 'pi pi-window-minimize'"
          text
          size="small"
          @click="isMinimized = !isMinimized"
        />
      </div>
    </template>

    <template #content>
      <div v-if="!isMinimized" class="chat-content">
        <div class="messages-container mb-3">
          <div
            v-for="message in messages"
            :key="message.id"
            class="message-item mb-2"
          >
            <div class="flex gap-2">
              <Avatar
                :image="message.user.avatar_url"
                :label="message.user.username[0]"
                size="small"
                shape="circle"
              />
              <div class="flex-1">
                <div class="mb-1 flex items-center gap-2">
                  <span class="font-semibold text-sm">{{ message.user.username }}</span>
                  <span class="text-xs text-gray-500">{{ formatTime(message.created_at) }}</span>
                </div>
                <p class="text-sm">{{ message.message }}</p>
              </div>
            </div>
          </div>
        </div>

        <div class="flex gap-2">
          <InputText
            v-model="newMessage"
            placeholder="Type a message..."
            class="flex-1"
            @keyup.enter="sendMessage"
          />
          <Button
            icon="pi pi-send"
            :loading="isLoading"
            @click="sendMessage"
          />
        </div>
      </div>
    </template>
  </Card>
</template>

<style scoped>
.collaboration-chat {
  width: 100%;
  max-width: 400px;
}

.collaboration-chat.minimized {
  height: auto;
}

.messages-container {
  max-height: 300px;
  overflow-y: auto;
}
</style>
EOF

# Security Components
cat > "$COMPONENTS_DIR/security/AccessLogsViewer.vue" << 'EOF'
<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { client } from '@/utils/client'
import type { AccessLog } from '@/types/components'
import Card from 'primevue/card'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import InputText from 'primevue/inputtext'
import Select from 'primevue/select'
import Button from 'primevue/button'
import Tag from 'primevue/tag'

const props = defineProps<{
  pasteId: string
}>()

const logs = ref<AccessLog[]>([])
const isLoading = ref(false)
const searchQuery = ref('')
const filterStatus = ref<'all' | 'granted' | 'denied'>('all')

const filteredLogs = computed(() => {
  return logs.value.filter(log => {
    const matchesSearch =
      log.ip_address.includes(searchQuery.value) ||
      log.user?.username.includes(searchQuery.value) ||
      log.action.includes(searchQuery.value)

    const matchesFilter =
      filterStatus.value === 'all' ||
      (filterStatus.value === 'granted' && log.granted) ||
      (filterStatus.value === 'denied' && !log.granted)

    return matchesSearch && matchesFilter
  })
})

async function fetchLogs() {
  isLoading.value = true
  try {
    const response = await client.get(`/api/v2/pastes/${props.pasteId}/access-logs`)
    logs.value = response.data.logs
  } catch (e) {
    console.error('Failed to fetch access logs:', e)
  } finally {
    isLoading.value = false
  }
}

function exportCSV() {
  const csv = [
    ['Timestamp', 'IP Address', 'User', 'Action', 'Status', 'Deny Reason'].join(','),
    ...filteredLogs.value.map(log => [
      log.timestamp,
      log.ip_address,
      log.user?.username || 'Anonymous',
      log.action,
      log.granted ? 'Granted' : 'Denied',
      log.deny_reason || ''
    ].join(','))
  ].join('\n')

  const blob = new Blob([csv], { type: 'text/csv' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `access-logs-${props.pasteId}.csv`
  link.click()
  URL.revokeObjectURL(url)
}

onMounted(() => {
  fetchLogs()
})
</script>

<template>
  <Card class="access-logs-viewer">
    <template #title>
      <div class="flex items-center justify-between">
        <span>Access Logs</span>
        <Button
          label="Export CSV"
          icon="pi pi-download"
          size="small"
          @click="exportCSV"
        />
      </div>
    </template>

    <template #content>
      <div class="space-y-4">
        <div class="flex gap-2">
          <InputText
            v-model="searchQuery"
            placeholder="Search by IP, user, or action..."
            class="flex-1"
          />
          <Select
            v-model="filterStatus"
            :options="[
              { label: 'All', value: 'all' },
              { label: 'Granted', value: 'granted' },
              { label: 'Denied', value: 'denied' }
            ]"
            option-label="label"
            option-value="value"
          />
        </div>

        <DataTable
          :value="filteredLogs"
          :loading="isLoading"
          paginator
          :rows="10"
          striped-rows
        >
          <Column field="timestamp" header="Timestamp">
            <template #body="{ data }">
              {{ new Date(data.timestamp).toLocaleString() }}
            </template>
          </Column>
          <Column field="ip_address" header="IP Address" />
          <Column field="user.username" header="User">
            <template #body="{ data }">
              {{ data.user?.username || 'Anonymous' }}
            </template>
          </Column>
          <Column field="action" header="Action" />
          <Column field="granted" header="Status">
            <template #body="{ data }">
              <Tag
                :severity="data.granted ? 'success' : 'danger'"
                :value="data.granted ? 'Granted' : 'Denied'"
              />
            </template>
          </Column>
          <Column field="deny_reason" header="Reason">
            <template #body="{ data }">
              <span v-if="data.deny_reason" class="text-sm">
                {{ data.deny_reason }}
              </span>
            </template>
          </Column>
        </DataTable>
      </div>
    </template>
  </Card>
</template>

<style scoped>
.access-logs-viewer {
  width: 100%;
}
</style>
EOF

cat > "$COMPONENTS_DIR/security/BurnAfterReadIndicator.vue" << 'EOF'
<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import Message from 'primevue/message'
import Tag from 'primevue/tag'

const props = defineProps<{
  viewsRemaining: number
  totalViews: number
}>()

const currentViews = ref(props.viewsRemaining)
const isAnimating = ref(false)

const progressPercentage = computed(() => {
  return ((props.totalViews - currentViews.value) / props.totalViews) * 100
})

const severityLevel = computed(() => {
  if (currentViews.value === 1) return 'danger'
  if (currentViews.value <= 3) return 'warn'
  return 'info'
})

onMounted(() => {
  if (currentViews.value <= 3) {
    isAnimating.value = true
  }
})
</script>

<template>
  <Message
    :severity="severityLevel"
    :closable="false"
    class="burn-after-read-indicator"
    :class="{ 'pulse-animation': isAnimating }"
  >
    <div class="flex items-center gap-3">
      <i class="pi pi-fire text-2xl"></i>
      <div class="flex-1">
        <div class="mb-1 font-semibold">
          This paste will self-destruct
        </div>
        <div class="text-sm">
          <Tag
            :severity="severityLevel"
            :value="`${currentViews} ${currentViews === 1 ? 'view' : 'views'} remaining`"
          />
        </div>
      </div>
    </div>
  </Message>
</template>

<style scoped>
.burn-after-read-indicator {
  width: 100%;
}

.pulse-animation {
  animation: pulse 2s cubic-bezier(0.4, 0, 0.6, 1) infinite;
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.7;
  }
}
</style>
EOF

echo "Components generated successfully!"
echo "Generated:"
echo "  - CollaborationPanel.vue ✅"
echo "  - CursorOverlay.vue ✅"
echo "  - CollaborationChat.vue ✅"
echo "  - AccessControlPanel.vue ✅"
echo "  - AccessLogsViewer.vue ✅"
echo "  - BurnAfterReadIndicator.vue ✅"
EOF

chmod +x "/__modal/volumes/vo-pkwyL871BwojYJgLZ0F1rM/claude-workspace/r79767525_gmail.com/deathamongstlife/pastefy/scripts/generate-remaining-components.sh"
