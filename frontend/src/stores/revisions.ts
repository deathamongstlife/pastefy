import { ref } from 'vue'
import { defineStore } from 'pinia'
import type { PasteRevision, PasteBranch, RevisionCreateRequest, BranchCreateRequest } from '@/types/revision'
import { client } from '@/main'

export const useRevisionsStore = defineStore('revisions', () => {
  const revisions = ref<PasteRevision[]>([])
  const branches = ref<PasteBranch[]>([])
  const currentRevision = ref<PasteRevision | undefined>(undefined)
  const loading = ref(false)

  async function fetchRevisions(pasteKey: string) {
    loading.value = true
    try {
      const response = await client.get(`/api/v2/paste/${pasteKey}/revisions`)
      revisions.value = response.data
    } catch (error) {
      console.error('Failed to fetch revisions:', error)
      revisions.value = []
    }
    loading.value = false
  }

  async function fetchBranches(pasteKey: string) {
    loading.value = true
    try {
      const response = await client.get(`/api/v2/paste/${pasteKey}/revisions/branches`)
      branches.value = response.data
    } catch (error) {
      console.error('Failed to fetch branches:', error)
      branches.value = []
    }
    loading.value = false
  }

  async function createRevision(pasteKey: string, request: RevisionCreateRequest) {
    loading.value = true
    try {
      await client.post(`/api/v2/paste/${pasteKey}/revisions`, request)
      await fetchRevisions(pasteKey)
      return true
    } catch (error) {
      console.error('Failed to create revision:', error)
      return false
    } finally {
      loading.value = false
    }
  }

  async function createBranch(pasteKey: string, request: BranchCreateRequest) {
    loading.value = true
    try {
      const response = await client.post(`/api/v2/paste/${pasteKey}/revisions/branches`, request)
      await fetchBranches(pasteKey)
      return response.data
    } catch (error) {
      console.error('Failed to create branch:', error)
      return null
    } finally {
      loading.value = false
    }
  }

  async function rollbackRevision(pasteKey: string, revisionId: string) {
    loading.value = true
    try {
      await client.post(`/api/v2/paste/${pasteKey}/revisions/${revisionId}/rollback`)
      await fetchRevisions(pasteKey)
      return true
    } catch (error) {
      console.error('Failed to rollback:', error)
      return false
    } finally {
      loading.value = false
    }
  }

  function reset() {
    revisions.value = []
    branches.value = []
    currentRevision.value = undefined
  }

  return {
    revisions,
    branches,
    currentRevision,
    loading,
    fetchRevisions,
    fetchBranches,
    createRevision,
    createBranch,
    rollbackRevision,
    reset
  }
})
