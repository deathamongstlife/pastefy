<script setup lang="ts">
import { ref, computed } from 'vue'
import { client } from '@/utils/client'
import Button from 'primevue/button'
import Textarea from 'primevue/textarea'
import Card from 'primevue/card'
import Message from 'primevue/message'
import ProgressBar from 'primevue/progressbar'
import Knob from 'primevue/knob'

type QualityMetric = {
  name: string
  score: number
  description: string
}

type Suggestion = {
  id: string
  title: string
  description: string
  impact: 'HIGH' | 'MEDIUM' | 'LOW'
  category: string
}

type QualityAnalysis = {
  overall_score: number
  readability: number
  maintainability: number
  complexity: number
  metrics: QualityMetric[]
  suggestions: Suggestion[]
}

const props = defineProps<{
  pasteId?: string
  initialCode?: string
}>()

const emit = defineEmits<{
  analyzed: [analysis: QualityAnalysis]
}>()

const code = ref(props.initialCode || '')
const analysis = ref<QualityAnalysis | null>(null)
const isLoading = ref(false)
const error = ref<string | null>(null)

const canAnalyze = computed(() => code.value.trim().length > 0)
const hasAnalysis = computed(() => analysis.value !== null)

const scoreColor = computed(() => {
  if (!analysis.value) return 'var(--primary-color)'
  const score = analysis.value.overall_score
  if (score >= 80) return '#22c55e'
  if (score >= 60) return '#eab308'
  return '#ef4444'
})

async function analyzeQuality() {
  if (!canAnalyze.value) return

  isLoading.value = true
  error.value = null

  try {
    const response = await client.post<QualityAnalysis>('/api/v2/ai/analyze-quality', {
      code: code.value,
      paste_id: props.pasteId
    })

    analysis.value = response.data
    emit('analyzed', analysis.value)
  } catch (e) {
    error.value = 'Failed to analyze code quality. Please try again.'
    console.error('Quality analysis error:', e)
  } finally {
    isLoading.value = false
  }
}

function getImpactSeverity(impact: string): 'danger' | 'warn' | 'info' {
  switch (impact) {
    case 'HIGH':
      return 'danger'
    case 'MEDIUM':
      return 'warn'
    case 'LOW':
      return 'info'
    default:
      return 'info'
  }
}

function getImpactIcon(impact: string): string {
  switch (impact) {
    case 'HIGH':
      return 'pi-exclamation-circle'
    case 'MEDIUM':
      return 'pi-exclamation-triangle'
    case 'LOW':
      return 'pi-info-circle'
    default:
      return 'pi-info-circle'
  }
}

function clearAnalysis() {
  analysis.value = null
  error.value = null
}
</script>

<template>
  <Card class="quality-analyzer">
    <template #title>
      <div class="flex items-center justify-between">
        <span>Code Quality Analyzer</span>
        <i class="pi pi-chart-line text-green-500"></i>
      </div>
    </template>

    <template #content>
      <div class="space-y-4">
        <div class="flex flex-col">
          <label for="code-input" class="mb-2 text-sm font-medium">
            Code to Analyze
          </label>
          <Textarea
            id="code-input"
            v-model="code"
            :disabled="isLoading"
            rows="12"
            placeholder="Paste your code here..."
            class="font-mono text-sm"
          />
        </div>

        <div class="flex gap-2">
          <Button
            label="Analyze Quality"
            icon="pi pi-chart-line"
            :loading="isLoading"
            :disabled="!canAnalyze"
            @click="analyzeQuality"
          />
          <Button
            v-if="hasAnalysis"
            label="Clear"
            icon="pi pi-times"
            severity="secondary"
            outlined
            @click="clearAnalysis"
          />
        </div>

        <Message v-if="error" severity="error" :closable="false">
          {{ error }}
        </Message>

        <div v-if="hasAnalysis" class="analysis-results">
          <div class="mb-6 text-center">
            <h3 class="mb-4 text-lg font-semibold">Overall Quality Score</h3>
            <Knob
              v-model="analysis.overall_score"
              :readonly="true"
              :size="150"
              :stroke-width="10"
              :value-color="scoreColor"
            />
          </div>

          <div class="mb-6 grid grid-cols-1 gap-4 md:grid-cols-3">
            <Card>
              <template #content>
                <div class="text-center">
                  <div class="mb-2 text-sm font-medium text-gray-600 dark:text-gray-400">
                    Readability
                  </div>
                  <div class="text-3xl font-bold">
                    {{ analysis.readability }}%
                  </div>
                  <ProgressBar
                    :value="analysis.readability"
                    :show-value="false"
                    class="mt-2"
                  />
                </div>
              </template>
            </Card>

            <Card>
              <template #content>
                <div class="text-center">
                  <div class="mb-2 text-sm font-medium text-gray-600 dark:text-gray-400">
                    Maintainability
                  </div>
                  <div class="text-3xl font-bold">
                    {{ analysis.maintainability }}%
                  </div>
                  <ProgressBar
                    :value="analysis.maintainability"
                    :show-value="false"
                    class="mt-2"
                  />
                </div>
              </template>
            </Card>

            <Card>
              <template #content>
                <div class="text-center">
                  <div class="mb-2 text-sm font-medium text-gray-600 dark:text-gray-400">
                    Complexity
                  </div>
                  <div class="text-3xl font-bold">
                    {{ 100 - analysis.complexity }}%
                  </div>
                  <ProgressBar
                    :value="100 - analysis.complexity"
                    :show-value="false"
                    class="mt-2"
                  />
                  <div class="mt-1 text-xs text-gray-500">
                    Lower is better
                  </div>
                </div>
              </template>
            </Card>
          </div>

          <div v-if="analysis.suggestions.length > 0" class="suggestions-section">
            <h3 class="mb-3 text-lg font-semibold">Improvement Suggestions</h3>
            <div class="space-y-3">
              <Card
                v-for="suggestion in analysis.suggestions"
                :key="suggestion.id"
                class="suggestion-card"
              >
                <template #content>
                  <div class="flex items-start gap-3">
                    <i
                      :class="['pi', getImpactIcon(suggestion.impact), 'mt-1 text-xl']"
                      :style="{ color: getImpactSeverity(suggestion.impact) === 'danger' ? '#ef4444' : getImpactSeverity(suggestion.impact) === 'warn' ? '#eab308' : '#3b82f6' }"
                    ></i>
                    <div class="flex-1">
                      <div class="mb-1 flex items-center justify-between">
                        <h4 class="font-semibold">{{ suggestion.title }}</h4>
                        <span
                          :class="[
                            'rounded px-2 py-1 text-xs font-medium',
                            suggestion.impact === 'HIGH' ? 'bg-red-100 text-red-700 dark:bg-red-900 dark:text-red-300' :
                            suggestion.impact === 'MEDIUM' ? 'bg-yellow-100 text-yellow-700 dark:bg-yellow-900 dark:text-yellow-300' :
                            'bg-blue-100 text-blue-700 dark:bg-blue-900 dark:text-blue-300'
                          ]"
                        >
                          {{ suggestion.impact }}
                        </span>
                      </div>
                      <p class="text-sm text-gray-700 dark:text-gray-300">
                        {{ suggestion.description }}
                      </p>
                      <div class="mt-2 text-xs text-gray-500">
                        Category: {{ suggestion.category }}
                      </div>
                    </div>
                  </div>
                </template>
              </Card>
            </div>
          </div>
        </div>
      </div>
    </template>
  </Card>
</template>

<style scoped>
.quality-analyzer {
  width: 100%;
}

.analysis-results {
  margin-top: 1.5rem;
  padding-top: 1.5rem;
  border-top: 1px solid var(--surface-border);
}

.suggestions-section {
  margin-top: 1.5rem;
  padding-top: 1.5rem;
  border-top: 1px solid var(--surface-border);
}

.suggestion-card {
  transition: transform 0.2s;
}

.suggestion-card:hover {
  transform: translateX(4px);
}
</style>
