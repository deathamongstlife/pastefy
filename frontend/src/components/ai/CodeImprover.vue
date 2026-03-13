<script setup lang="ts">
import { ref, computed } from 'vue'
import { client } from '@/utils/client'
import Button from 'primevue/button'
import Textarea from 'primevue/textarea'
import Card from 'primevue/card'
import Message from 'primevue/message'
import Checkbox from 'primevue/checkbox'

type Improvement = {
  id: string
  title: string
  description: string
  original: string
  improved: string
  accepted: boolean
}

type ImprovementResponse = {
  improvements: Improvement[]
  improved_code: string
}

const props = defineProps<{
  pasteId?: string
  initialCode?: string
}>()

const emit = defineEmits<{
  improved: [code: string]
  applied: [code: string]
}>()

const originalCode = ref(props.initialCode || '')
const improvedCode = ref('')
const improvements = ref<Improvement[]>([])
const isLoading = ref(false)
const error = ref<string | null>(null)
const viewMode = ref<'split' | 'diff'>('split')

const canImprove = computed(() => originalCode.value.trim().length > 0)
const hasImprovements = computed(() => improvements.value.length > 0)
const selectedImprovements = computed(() =>
  improvements.value.filter(i => i.accepted)
)

async function getSuggestions() {
  if (!canImprove.value) return

  isLoading.value = true
  error.value = null

  try {
    const response = await client.post<ImprovementResponse>('/api/v2/ai/improve', {
      code: originalCode.value,
      paste_id: props.pasteId
    })

    improvements.value = response.data.improvements.map((imp, idx) => ({
      ...imp,
      id: `imp-${idx}`,
      accepted: true
    }))
    improvedCode.value = response.data.improved_code
    emit('improved', improvedCode.value)
  } catch (e) {
    error.value = 'Failed to get improvement suggestions. Please try again.'
    console.error('Code improvement error:', e)
  } finally {
    isLoading.value = false
  }
}

function toggleImprovement(improvement: Improvement) {
  improvement.accepted = !improvement.accepted
  updateImprovedCode()
}

function updateImprovedCode() {
  let code = originalCode.value
  selectedImprovements.value.forEach(imp => {
    code = code.replace(imp.original, imp.improved)
  })
  improvedCode.value = code
}

function acceptAll() {
  improvements.value.forEach(imp => {
    imp.accepted = true
  })
  updateImprovedCode()
}

function rejectAll() {
  improvements.value.forEach(imp => {
    imp.accepted = false
  })
  improvedCode.value = originalCode.value
}

function applyImprovements() {
  emit('applied', improvedCode.value)
}

function clearImprovements() {
  improvements.value = []
  improvedCode.value = ''
  error.value = null
}
</script>

<template>
  <Card class="code-improver">
    <template #title>
      <div class="flex items-center justify-between">
        <span>Code Improver</span>
        <i class="pi pi-sparkles text-indigo-500"></i>
      </div>
    </template>

    <template #content>
      <div class="space-y-4">
        <div class="flex flex-col">
          <label for="code-input" class="mb-2 text-sm font-medium">
            Original Code
          </label>
          <Textarea
            id="code-input"
            v-model="originalCode"
            :disabled="isLoading"
            rows="10"
            placeholder="Paste your code here..."
            class="font-mono text-sm"
          />
        </div>

        <div class="flex gap-2">
          <Button
            label="Get Suggestions"
            icon="pi pi-lightbulb"
            :loading="isLoading"
            :disabled="!canImprove"
            @click="getSuggestions"
          />
          <Button
            v-if="hasImprovements"
            label="Clear"
            icon="pi pi-times"
            severity="secondary"
            outlined
            @click="clearImprovements"
          />
        </div>

        <Message v-if="error" severity="error" :closable="false">
          {{ error }}
        </Message>

        <div v-if="hasImprovements" class="improvements-section">
          <div class="mb-4 flex items-center justify-between">
            <h3 class="text-lg font-semibold">
              Suggested Improvements ({{ selectedImprovements.length }}/{{ improvements.length }})
            </h3>
            <div class="flex gap-2">
              <Button
                label="Accept All"
                icon="pi pi-check"
                size="small"
                outlined
                @click="acceptAll"
              />
              <Button
                label="Reject All"
                icon="pi pi-times"
                size="small"
                outlined
                @click="rejectAll"
              />
            </div>
          </div>

          <div class="mb-4 space-y-3">
            <Card
              v-for="improvement in improvements"
              :key="improvement.id"
              :class="['improvement-card', { 'opacity-50': !improvement.accepted }]"
            >
              <template #content>
                <div class="flex items-start gap-3">
                  <Checkbox
                    v-model="improvement.accepted"
                    :binary="true"
                    @change="toggleImprovement(improvement)"
                  />
                  <div class="flex-1">
                    <h4 class="mb-1 font-semibold">{{ improvement.title }}</h4>
                    <p class="mb-2 text-sm text-gray-700 dark:text-gray-300">
                      {{ improvement.description }}
                    </p>
                    <div class="grid grid-cols-1 gap-2 md:grid-cols-2">
                      <div>
                        <div class="mb-1 text-xs font-medium text-red-600 dark:text-red-400">
                          Original
                        </div>
                        <pre class="rounded bg-red-50 p-2 text-xs dark:bg-red-900/20"><code>{{ improvement.original }}</code></pre>
                      </div>
                      <div>
                        <div class="mb-1 text-xs font-medium text-green-600 dark:text-green-400">
                          Improved
                        </div>
                        <pre class="rounded bg-green-50 p-2 text-xs dark:bg-green-900/20"><code>{{ improvement.improved }}</code></pre>
                      </div>
                    </div>
                  </div>
                </div>
              </template>
            </Card>
          </div>

          <div class="code-comparison">
            <div class="mb-2 flex items-center justify-between">
              <h3 class="text-lg font-semibold">Improved Code Preview</h3>
              <div class="flex gap-2">
                <Button
                  :label="viewMode === 'split' ? 'Split View' : 'Diff View'"
                  icon="pi pi-arrows-h"
                  size="small"
                  text
                  @click="viewMode = viewMode === 'split' ? 'diff' : 'split'"
                />
              </div>
            </div>

            <div v-if="viewMode === 'split'" class="grid grid-cols-1 gap-4 lg:grid-cols-2">
              <div>
                <div class="mb-1 text-sm font-medium">Original</div>
                <Textarea
                  v-model="originalCode"
                  :disabled="true"
                  rows="16"
                  class="font-mono text-sm"
                />
              </div>
              <div>
                <div class="mb-1 text-sm font-medium">Improved</div>
                <Textarea
                  v-model="improvedCode"
                  :disabled="true"
                  rows="16"
                  class="font-mono text-sm"
                />
              </div>
            </div>

            <div v-else>
              <Textarea
                v-model="improvedCode"
                :disabled="true"
                rows="16"
                class="font-mono text-sm"
              />
            </div>
          </div>

          <Button
            label="Apply Improvements"
            icon="pi pi-check"
            class="mt-4"
            :disabled="selectedImprovements.length === 0"
            @click="applyImprovements"
          />
        </div>
      </div>
    </template>
  </Card>
</template>

<style scoped>
.code-improver {
  width: 100%;
}

.improvements-section {
  margin-top: 1.5rem;
  padding-top: 1.5rem;
  border-top: 1px solid var(--surface-border);
}

.improvement-card {
  transition: all 0.2s;
}

.code-comparison {
  margin-top: 1.5rem;
  padding-top: 1.5rem;
  border-top: 1px solid var(--surface-border);
}
</style>
