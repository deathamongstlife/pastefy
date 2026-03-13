<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { client } from '@/utils/client'
import Card from 'primevue/card'
import Tag from 'primevue/tag'
import Button from 'primevue/button'

type ConnectionStatus = 'connected' | 'disconnected' | 'connecting' | 'error'

type AIStatus = {
  status: ConnectionStatus
  model: string
  provider: string
  response_time_ms: number
  requests_today: number
  quota_remaining: number
  error_message?: string
}

const status = ref<AIStatus>({
  status: 'connecting',
  model: '',
  provider: '',
  response_time_ms: 0,
  requests_today: 0,
  quota_remaining: 0
})

const isLoading = ref(false)
let statusCheckInterval: number | null = null

const statusSeverity = computed(() => {
  switch (status.value.status) {
    case 'connected':
      return 'success'
    case 'disconnected':
      return 'secondary'
    case 'connecting':
      return 'warn'
    case 'error':
      return 'danger'
    default:
      return 'secondary'
  }
})

const statusIcon = computed(() => {
  switch (status.value.status) {
    case 'connected':
      return 'pi-check-circle'
    case 'disconnected':
      return 'pi-times-circle'
    case 'connecting':
      return 'pi-spin pi-spinner'
    case 'error':
      return 'pi-exclamation-circle'
    default:
      return 'pi-question-circle'
  }
})

const statusLabel = computed(() => {
  switch (status.value.status) {
    case 'connected':
      return 'Connected'
    case 'disconnected':
      return 'Disconnected'
    case 'connecting':
      return 'Connecting...'
    case 'error':
      return 'Error'
    default:
      return 'Unknown'
  }
})

const responseTimeColor = computed(() => {
  const time = status.value.response_time_ms
  if (time < 1000) return 'text-green-600 dark:text-green-400'
  if (time < 3000) return 'text-yellow-600 dark:text-yellow-400'
  return 'text-red-600 dark:text-red-400'
})

async function checkStatus() {
  isLoading.value = true
  try {
    const response = await client.get<AIStatus>('/api/v2/ai/status')
    status.value = response.data
  } catch (e) {
    status.value.status = 'error'
    status.value.error_message = 'Failed to connect to AI service'
    console.error('AI status check error:', e)
  } finally {
    isLoading.value = false
  }
}

async function reconnect() {
  status.value.status = 'connecting'
  await checkStatus()
}

onMounted(() => {
  checkStatus()
  statusCheckInterval = window.setInterval(checkStatus, 30000)
})

onUnmounted(() => {
  if (statusCheckInterval) {
    clearInterval(statusCheckInterval)
  }
})
</script>

<template>
  <Card class="ai-status-indicator">
    <template #content>
      <div class="space-y-3">
        <div class="flex items-center justify-between">
          <div class="flex items-center gap-2">
            <i :class="['pi', statusIcon, 'text-xl']"></i>
            <Tag :severity="statusSeverity">{{ statusLabel }}</Tag>
          </div>
          <Button
            v-if="status.status !== 'connected'"
            icon="pi pi-refresh"
            label="Reconnect"
            size="small"
            :loading="isLoading"
            @click="reconnect"
          />
        </div>

        <div v-if="status.status === 'connected'" class="space-y-2 text-sm">
          <div class="flex justify-between">
            <span class="text-gray-600 dark:text-gray-400">Model</span>
            <span class="font-medium">{{ status.model }}</span>
          </div>
          <div class="flex justify-between">
            <span class="text-gray-600 dark:text-gray-400">Provider</span>
            <span class="font-medium">{{ status.provider }}</span>
          </div>
          <div class="flex justify-between">
            <span class="text-gray-600 dark:text-gray-400">Response Time</span>
            <span :class="['font-medium', responseTimeColor]">
              {{ status.response_time_ms }}ms
            </span>
          </div>
          <div class="flex justify-between">
            <span class="text-gray-600 dark:text-gray-400">Requests Today</span>
            <span class="font-medium">{{ status.requests_today }}</span>
          </div>
          <div class="flex justify-between">
            <span class="text-gray-600 dark:text-gray-400">Quota Remaining</span>
            <span class="font-medium">{{ status.quota_remaining }}</span>
          </div>
        </div>

        <div v-if="status.error_message" class="rounded bg-red-50 p-2 text-sm text-red-700 dark:bg-red-900/20 dark:text-red-300">
          <i class="pi pi-exclamation-triangle mr-2"></i>
          {{ status.error_message }}
        </div>
      </div>
    </template>
  </Card>
</template>

<style scoped>
.ai-status-indicator {
  width: 100%;
}
</style>
