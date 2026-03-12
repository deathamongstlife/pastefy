<script setup lang="ts">
import { ref, onMounted } from 'vue'
import Card from 'primevue/card'
import Timeline from 'primevue/timeline'
import Avatar from 'primevue/avatar'
import { client } from '@/utils/client'
import type { Activity } from '@/types/social'

const activities = ref<Activity[]>([])
const isLoading = ref(false)

async function loadFeed() {
  isLoading.value = true
  try {
    const response = await client.get('/api/v2/social/feed?limit=50')
    activities.value = response.data
  } catch (error) {
    console.error('Failed to load activity feed:', error)
  } finally {
    isLoading.value = false
  }
}

function getActivityIcon(type: string): string {
  switch (type) {
    case 'create_paste':
      return 'pi pi-file'
    case 'follow':
      return 'pi pi-user-plus'
    case 'comment':
      return 'pi pi-comment'
    case 'star':
      return 'pi pi-star'
    default:
      return 'pi pi-bolt'
  }
}

function getActivityMessage(activity: Activity): string {
  switch (activity.activity_type) {
    case 'create_paste':
      return 'created a new paste'
    case 'follow':
      return 'followed a user'
    case 'comment':
      return 'commented on a paste'
    case 'star':
      return 'starred a paste'
    default:
      return activity.activity_type
  }
}

function formatTime(timestamp: string): string {
  const date = new Date(timestamp)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const minutes = Math.floor(diff / 60000)
  
  if (minutes < 1) return 'just now'
  if (minutes < 60) return `${minutes}m ago`
  
  const hours = Math.floor(minutes / 60)
  if (hours < 24) return `${hours}h ago`
  
  const days = Math.floor(hours / 24)
  if (days < 7) return `${days}d ago`
  
  return date.toLocaleDateString()
}

onMounted(() => {
  loadFeed()
})
</script>

<template>
  <Card class="activity-feed border border-cyan-500/30 bg-gray-900/50 backdrop-blur">
    <template #title>
      <div class="flex items-center gap-2 text-cyan-400">
        <i class="pi pi-bolt"></i>
        Activity Feed
      </div>
    </template>

    <template #content>
      <div v-if="isLoading" class="flex justify-center py-8">
        <i class="pi pi-spin pi-spinner text-2xl text-cyan-400"></i>
      </div>

      <div v-else-if="activities.length === 0" class="py-8 text-center text-gray-400">
        <i class="pi pi-inbox mb-2 text-4xl"></i>
        <p>No activity yet</p>
        <p class="text-sm">Follow users to see their activity here</p>
      </div>

      <Timeline v-else :value="activities" class="activity-timeline">
        <template #marker="{ item }">
          <div class="flex h-10 w-10 items-center justify-center rounded-full border-2 border-cyan-500 bg-gray-800">
            <i :class="getActivityIcon(item.activity_type)" class="text-cyan-400"></i>
          </div>
        </template>

        <template #content="{ item }">
          <Card class="mb-4 border border-cyan-500/20 bg-gray-800/50">
            <template #content>
              <div class="space-y-2">
                <div class="flex items-center gap-2">
                  <Avatar
                    :label="item.user?.name?.charAt(0)"
                    shape="circle"
                    class="bg-gradient-to-br from-cyan-500 to-purple-600"
                  />
                  <div class="flex-1">
                    <p class="text-sm">
                      <span class="font-semibold text-cyan-400">{{ item.user?.username }}</span>
                      <span class="text-gray-400"> {{ getActivityMessage(item) }}</span>
                    </p>
                    <p class="text-xs text-gray-500">{{ formatTime(item.created_at) }}</p>
                  </div>
                </div>
              </div>
            </template>
          </Card>
        </template>
      </Timeline>
    </template>
  </Card>
</template>

<style scoped>
.activity-feed {
  max-height: 600px;
  overflow-y: auto;
}

.activity-timeline :deep(.p-timeline-event-connector) {
  background: linear-gradient(to bottom, #06b6d4, #8b5cf6);
}

.activity-feed::-webkit-scrollbar {
  width: 8px;
}

.activity-feed::-webkit-scrollbar-track {
  background: rgba(17, 24, 39, 0.5);
}

.activity-feed::-webkit-scrollbar-thumb {
  background: rgba(6, 182, 212, 0.3);
  border-radius: 4px;
}

.activity-feed::-webkit-scrollbar-thumb:hover {
  background: rgba(6, 182, 212, 0.5);
}
</style>
