import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import type {
  AIStatusResponse,
  AITextResponse,
  BugDetectionResponse,
  TagGenerationResponse,
  QualityAnalysisResponse,
  ImprovementSuggestionsResponse,
  AIRequest,
  TranslateCodeRequest,
  GenerateDocsRequest,
  GenerateTagsRequest,
} from '@/types/ai'
import { client } from '@/utils/client'

export const useAIStore = defineStore('ai', () => {
  const status = ref<AIStatusResponse | null>(null)
  const isLoading = ref(false)
  const error = ref<string | null>(null)

  const isAIEnabled = computed(() => status.value?.enabled === true)
  const isAIConnected = computed(() => status.value?.gateway_connected === true)

  async function checkStatus() {
    try {
      isLoading.value = true
      error.value = null
      const response = await client.get('/api/v2/ai/status')
      status.value = response.data
      return status.value
    } catch (e: any) {
      error.value = e.response?.data?.message || 'Failed to check AI status'
      status.value = {
        enabled: false,
        gateway_connected: false,
        agent_id: null,
        message: error.value,
      }
      return status.value
    } finally {
      isLoading.value = false
    }
  }

  async function explainCode(request: AIRequest): Promise<AITextResponse> {
    try {
      isLoading.value = true
      error.value = null
      const response = await client.post('/api/v2/ai/explain', request)
      return response.data
    } catch (e: any) {
      error.value = e.response?.data?.message || 'Failed to explain code'
      throw new Error(error.value)
    } finally {
      isLoading.value = false
    }
  }

  async function detectBugs(request: AIRequest): Promise<BugDetectionResponse> {
    try {
      isLoading.value = true
      error.value = null
      const response = await client.post('/api/v2/ai/detect-bugs', request)
      return response.data
    } catch (e: any) {
      error.value = e.response?.data?.message || 'Failed to detect bugs'
      throw new Error(error.value)
    } finally {
      isLoading.value = false
    }
  }

  async function generateTags(request: GenerateTagsRequest): Promise<TagGenerationResponse> {
    try {
      isLoading.value = true
      error.value = null
      const response = await client.post('/api/v2/ai/generate-tags', request)
      return response.data
    } catch (e: any) {
      error.value = e.response?.data?.message || 'Failed to generate tags'
      throw new Error(error.value)
    } finally {
      isLoading.value = false
    }
  }

  async function translateCode(request: TranslateCodeRequest): Promise<AITextResponse> {
    try {
      isLoading.value = true
      error.value = null
      const response = await client.post('/api/v2/ai/translate', request)
      return response.data
    } catch (e: any) {
      error.value = e.response?.data?.message || 'Failed to translate code'
      throw new Error(error.value)
    } finally {
      isLoading.value = false
    }
  }

  async function analyzeQuality(request: AIRequest): Promise<QualityAnalysisResponse> {
    try {
      isLoading.value = true
      error.value = null
      const response = await client.post('/api/v2/ai/quality', request)
      return response.data
    } catch (e: any) {
      error.value = e.response?.data?.message || 'Failed to analyze code quality'
      throw new Error(error.value)
    } finally {
      isLoading.value = false
    }
  }

  async function generateDocumentation(request: GenerateDocsRequest): Promise<AITextResponse> {
    try {
      isLoading.value = true
      error.value = null
      const response = await client.post('/api/v2/ai/docs', request)
      return response.data
    } catch (e: any) {
      error.value = e.response?.data?.message || 'Failed to generate documentation'
      throw new Error(error.value)
    } finally {
      isLoading.value = false
    }
  }

  async function suggestImprovements(request: AIRequest): Promise<ImprovementSuggestionsResponse> {
    try {
      isLoading.value = true
      error.value = null
      const response = await client.post('/api/v2/ai/improve', request)
      return response.data
    } catch (e: any) {
      error.value = e.response?.data?.message || 'Failed to suggest improvements'
      throw new Error(error.value)
    } finally {
      isLoading.value = false
    }
  }

  return {
    status,
    isLoading,
    error,
    isAIEnabled,
    isAIConnected,
    checkStatus,
    explainCode,
    detectBugs,
    generateTags,
    translateCode,
    analyzeQuality,
    generateDocumentation,
    suggestImprovements,
  }
})
