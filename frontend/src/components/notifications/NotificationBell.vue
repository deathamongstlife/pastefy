<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { client } from '@/utils/client'
import type { UserNotification } from '@/types/components'
import Button from 'primevue/button'
import Badge from 'primevue/badge'
import OverlayPanel from 'primevue/overlaypanel'
import Avatar from 'primevue/avatar'
import Divider from 'primevue/divider'

const router = useRouter()

const notifications = ref<UserNotification[]>([])
const isLoading = ref(false)
const overlayPanel = ref()

const unreadCount = computed(() =>
  notifications.value.filter(n => !n.read).length
)

const recentNotifications = computed(() =>
  notifications.value.slice(0, 5)
)

async function fetchNotifications() {
  isLoading.value = true
  try {
    const response = await client.get<{ notifications: UserNotification[] }>(
      '/api/v2/notifications?limit=5'
    )
    notifications.value = response.data.notifications
  } catch (e) {
    console.error('Failed to fetch notifications:', e)
  } finally {
    isLoading.value = false
  }
}

async function markAsRead(notificationId: string) {
  try {
    await client.put(`/api/v2/notifications/${notificationId}/read`)
    const notification = notifications.value.find(n => n.id === notificationId)
    if (notification) {
      notification.read = true
    }
  } catch (e) {
    console.error('Failed to mark notification as read:', e)
  }
}

async function markAllAsRead() {
  try {
    await client.put('/api/v2/notifications/mark-all-read')
    notifications.value.forEach(n => {
      n.read = true
    })
  } catch (e) {
    console.error('Failed to mark all as read:', e)
  }
}

function handleNotificationClick(notification: UserNotification) {
  markAsRead(notification.id)
  if (notification.action_url) {
    router.push(notification.action_url)
  }
  overlayPanel.value.hide()
}

function viewAllNotifications() {
  router.push('/notifications')
  overlayPanel.value.hide()
}

function getNotificationIcon(type: string): string {
  switch (type) {
    case 'COMMENT':
      return 'pi-comment'
    case 'FOLLOW':
      return 'pi-user-plus'
    case 'STAR':
      return 'pi-star'
    case 'MENTION':
      return 'pi-at'
    case 'PASTE':
      return 'pi-file'
    case 'SYSTEM':
      return 'pi-info-circle'
    default:
      return 'pi-bell'
  }
}

function formatTime(dateString: string): string {
  const date = new Date(dateString)
  const now = new Date()
  const diffMs = now.getTime() - date.getTime()
  const diffMins = Math.floor(diffMs / 60000)

  if (diffMins < 1) return 'Just now'
  if (diffMins < 60) return `${diffMins}m ago`
  if (diffMins < 1440) return `${Math.floor(diffMins / 60)}h ago`
  if (diffMins < 10080) return `${Math.floor(diffMins / 1440)}d ago`
  return date.toLocaleDateString()
}

function toggle(event: Event) {
  overlayPanel.value.toggle(event)
  if (!overlayPanel.value.visible) {
    fetchNotifications()
  }
}

onMounted(() => {
  fetchNotifications()
  setInterval(fetchNotifications, 60000)
})
</script>

<template>
  <div class="notification-bell">
    <Button
      type="button"
      icon="pi pi-bell"
      severity="secondary"
      text
      @click="toggle"
      v-badge="unreadCount > 0 ? unreadCount : null"
      class="relative"
    />

    <OverlayPanel ref="overlayPanel" :style="{ width: '400px' }">
      <div class="notification-panel">
        <div class="mb-3 flex items-center justify-between">
          <h3 class="text-lg font-semibold">Notifications</h3>
          <Button
            v-if="unreadCount > 0"
            label="Mark all as read"
            size="small"
            text
            @click="markAllAsRead"
          />
        </div>

        <div v-if="isLoading" class="py-4 text-center">
          <i class="pi pi-spin pi-spinner text-2xl"></i>
        </div>

        <div v-else-if="recentNotifications.length === 0" class="py-8 text-center text-gray-500">
          <i class="pi pi-bell-slash mb-2 text-4xl"></i>
          <p>No notifications</p>
        </div>

        <div v-else class="space-y-2">
          <div
            v-for="notification in recentNotifications"
            :key="notification.id"
            :class="[
              'notification-item cursor-pointer rounded p-3 transition-colors',
              notification.read ? 'opacity-60' : 'bg-blue-50 dark:bg-blue-900/20',
              'hover:bg-gray-100 dark:hover:bg-gray-800'
            ]"
            @click="handleNotificationClick(notification)"
          >
            <div class="flex gap-3">
              <Avatar
                v-if="notification.actor"
                :image="notification.actor.avatar_url"
                :label="notification.actor.username[0].toUpperCase()"
                shape="circle"
              />
              <i
                v-else
                :class="['pi', getNotificationIcon(notification.type), 'text-xl']"
              ></i>

              <div class="flex-1">
                <div class="mb-1 font-medium">{{ notification.title }}</div>
                <div class="mb-2 text-sm text-gray-600 dark:text-gray-400">
                  {{ notification.message }}
                </div>
                <div class="flex items-center justify-between text-xs text-gray-500">
                  <span>{{ formatTime(notification.created_at) }}</span>
                  <i v-if="!notification.read" class="pi pi-circle-fill text-[6px] text-blue-500"></i>
                </div>
              </div>
            </div>
          </div>
        </div>

        <Divider />

        <Button
          label="View All Notifications"
          icon="pi pi-arrow-right"
          text
          class="w-full"
          @click="viewAllNotifications"
        />
      </div>
    </OverlayPanel>
  </div>
</template>

<style scoped>
.notification-bell {
  position: relative;
}

.notification-panel {
  max-height: 600px;
  overflow-y: auto;
}
</style>
