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
