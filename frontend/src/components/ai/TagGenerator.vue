<script setup lang="ts">
import { ref, computed } from 'vue'
import { client } from '@/utils/client'
import Button from 'primevue/button'
import Chip from 'primevue/chip'
import InputText from 'primevue/inputtext'
import Card from 'primevue/card'
import Message from 'primevue/message'

const props = defineProps<{
  pasteId: string
  code: string
  existingTags?: string[]
}>()

const emit = defineEmits<{
  tagsGenerated: [tags: string[]]
  tagsApplied: [tags: string[]]
}>()

const generatedTags = ref<string[]>([])
const customTag = ref('')
const isLoading = ref(false)
const isApplying = ref(false)
const error = ref<string | null>(null)
const selectedTags = ref<Set<string>>(new Set(props.existingTags || []))

const allTags = computed(() => {
  const combined = new Set([...generatedTags.value, ...(props.existingTags || [])])
  return Array.from(combined)
})

const hasGeneratedTags = computed(() => generatedTags.value.length > 0)
const hasSelectedTags = computed(() => selectedTags.value.size > 0)

async function generateTags() {
  isLoading.value = true
  error.value = null

  try {
    const response = await client.post('/api/v2/ai/generate-tags', {
      paste_id: props.pasteId,
      code: props.code
    })

    generatedTags.value = response.data.tags
    generatedTags.value.forEach(tag => selectedTags.value.add(tag))
    emit('tagsGenerated', generatedTags.value)
  } catch (e) {
    error.value = 'Failed to generate tags. Please try again.'
    console.error('Tag generation error:', e)
  } finally {
    isLoading.value = false
  }
}

function toggleTag(tag: string) {
  if (selectedTags.value.has(tag)) {
    selectedTags.value.delete(tag)
  } else {
    selectedTags.value.add(tag)
  }
}

function removeTag(tag: string) {
  selectedTags.value.delete(tag)
  generatedTags.value = generatedTags.value.filter(t => t !== tag)
}

function addCustomTag() {
  const tag = customTag.value.trim()
  if (tag && !selectedTags.value.has(tag)) {
    generatedTags.value.push(tag)
    selectedTags.value.add(tag)
    customTag.value = ''
  }
}

function handleKeyPress(event: KeyboardEvent) {
  if (event.key === 'Enter') {
    addCustomTag()
  }
}

async function applyTags() {
  isApplying.value = true
  error.value = null

  try {
    await client.put(`/api/v2/pastes/${props.pasteId}/tags`, {
      tags: Array.from(selectedTags.value)
    })

    emit('tagsApplied', Array.from(selectedTags.value))
  } catch (e) {
    error.value = 'Failed to apply tags. Please try again.'
    console.error('Apply tags error:', e)
  } finally {
    isApplying.value = false
  }
}

function isTagSelected(tag: string): boolean {
  return selectedTags.value.has(tag)
}

function isTagGenerated(tag: string): boolean {
  return generatedTags.value.includes(tag)
}
</script>

<template>
  <Card class="tag-generator">
    <template #title>
      <div class="flex items-center justify-between">
        <span>AI Tag Generator</span>
        <i class="pi pi-tags text-purple-500"></i>
      </div>
    </template>

    <template #content>
      <div class="space-y-4">
        <div class="code-preview">
          <label class="mb-2 block text-sm font-medium">Code Preview</label>
          <div class="max-h-32 overflow-y-auto rounded border border-gray-300 bg-gray-50 p-3 dark:border-gray-700 dark:bg-gray-900">
            <pre class="font-mono text-xs">{{ code.substring(0, 500) }}{{ code.length > 500 ? '...' : '' }}</pre>
          </div>
        </div>

        <Button
          label="Generate Tags"
          icon="pi pi-sparkles"
          :loading="isLoading"
          @click="generateTags"
        />

        <Message v-if="error" severity="error" :closable="false">
          {{ error }}
        </Message>

        <div v-if="hasGeneratedTags" class="generated-tags">
          <h3 class="mb-2 text-sm font-semibold">Generated Tags</h3>
          <div class="flex flex-wrap gap-2">
            <Chip
              v-for="tag in allTags"
              :key="tag"
              :label="tag"
              :class="[
                'cursor-pointer transition-all',
                isTagSelected(tag) ? 'bg-blue-500 text-white' : 'bg-gray-200 dark:bg-gray-700'
              ]"
              :removable="true"
              @click="toggleTag(tag)"
              @remove="removeTag(tag)"
            >
              <template #removeicon>
                <i class="pi pi-times ml-2"></i>
              </template>
            </Chip>
          </div>

          <div class="mt-3 text-xs text-gray-600 dark:text-gray-400">
            <i class="pi pi-info-circle"></i>
            Click tags to select/deselect. {{ selectedTags.size }} tag(s) selected.
          </div>
        </div>

        <div class="custom-tag-input">
          <label for="custom-tag" class="mb-2 block text-sm font-medium">
            Add Custom Tag
          </label>
          <div class="flex gap-2">
            <InputText
              id="custom-tag"
              v-model="customTag"
              placeholder="Enter tag name"
              class="flex-1"
              @keypress="handleKeyPress"
            />
            <Button
              label="Add"
              icon="pi pi-plus"
              :disabled="!customTag.trim()"
              @click="addCustomTag"
            />
          </div>
        </div>

        <div v-if="hasSelectedTags" class="apply-section">
          <Button
            label="Apply Tags to Paste"
            icon="pi pi-check"
            :loading="isApplying"
            @click="applyTags"
          />
        </div>
      </div>
    </template>
  </Card>
</template>

<style scoped>
.tag-generator {
  width: 100%;
}

.generated-tags {
  padding-top: 1rem;
  border-top: 1px solid var(--surface-border);
}

.apply-section {
  padding-top: 1rem;
  border-top: 1px solid var(--surface-border);
}
</style>
