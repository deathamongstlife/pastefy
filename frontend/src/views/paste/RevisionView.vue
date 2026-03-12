<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { client } from '@/utils/client'
import type { PasteRevision } from '@/types/version-control'
import Card from 'primevue/card'
import Button from 'primevue/button'
import { useClipboard } from '@vueuse/core'

const route = useRoute()
const router = useRouter()
const pasteKey = route.params.id as string
const revisionId = route.params.revisionId as string

const revision = ref<PasteRevision | null>(null)
const content = ref<string>('')
const isLoading = ref(false)

const clipboard = useClipboard()

async function loadRevision() {
  isLoading.value = true
  try {
    const [revisionRes, contentRes] = await Promise.all([
      client.get(`/api/v2/paste/${pasteKey}/revisions/${revisionId}`),
      client.get(`/api/v2/paste/${pasteKey}/revisions/${revisionId}/content`),
    ])
    revision.value = revisionRes.data
    content.value = contentRes.data.content
  } catch (error) {
    console.error('Failed to load revision:', error)
  } finally {
    isLoading.value = false
  }
}

function copyContent() {
  clipboard.copy(content.value)
  alert('Content copied to clipboard')
}

function goBack() {
  router.push(`/paste/${pasteKey}/history`)
}

function formatDate(dateString: string) {
  return new Date(dateString).toLocaleString()
}

onMounted(() => {
  loadRevision()
})
</script>

<template>
  <div class="min-h-screen bg-dark-bg p-8">
    <div class="mx-auto max-w-6xl">
      <Card class="cyber-card mb-8">
        <template #title>
          <div class="flex items-center justify-between">
            <h1 class="neon-text text-3xl font-bold text-cyber-blue">REVISION VIEW</h1>
            <Button label="Back to History" icon="ti ti-arrow-left" outlined @click="goBack" />
          </div>
        </template>

        <template #content>
          <div v-if="isLoading" class="text-center text-text-secondary">Loading revision...</div>

          <div v-else-if="revision">
            <div class="mb-6 rounded-lg border border-cyber-pink/30 bg-black/50 p-4">
              <div class="mb-2 flex items-center justify-between">
                <span class="font-bold text-cyber-pink">Revision v{{ revision.revision_number }}</span>
                <span class="text-sm text-text-muted">
                  {{ formatDate(revision.created_at) }}
                </span>
              </div>

              <p class="mb-2 text-text-primary">
                <strong>Message:</strong> {{ revision.commit_message }}
              </p>

              <p class="text-text-secondary">
                <strong>Author:</strong> {{ revision.author.name }}
              </p>
            </div>

            <div class="mb-4 flex items-center justify-between">
              <h2 class="text-xl font-bold text-cyber-blue">Content</h2>
              <Button
                label="Copy"
                icon="ti ti-copy"
                size="small"
                outlined
                @click="copyContent"
              />
            </div>

            <pre
              class="overflow-auto rounded-lg border border-cyber-blue/30 bg-black p-4 text-sm text-text-primary"
            ><code>{{ content }}</code></pre>
          </div>

          <div v-else class="text-center text-text-secondary">Failed to load revision</div>
        </template>
      </Card>
    </div>
  </div>
</template>

<style scoped>
.cyber-card {
  background: rgba(13, 17, 23, 0.95);
  border: 1px solid #8b00ff;
  box-shadow: 0 0 20px rgba(139, 0, 255, 0.3);
}

.neon-text {
  text-shadow: 0 0 10px #00f5ff, 0 0 20px #00f5ff;
}

pre {
  max-height: 600px;
}

code {
  font-family: 'Fira Code', monospace;
}
</style>
