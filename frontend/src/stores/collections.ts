import { ref } from 'vue'
import { defineStore } from 'pinia'
import type {
  PasteCollection,
  CollectionWithPastes,
  CollectionCreateRequest,
  CollectionUpdateRequest,
  CollectionAddPasteRequest
} from '@/types/collection'
import { client } from '@/main'

export const useCollectionsStore = defineStore('collections', () => {
  const collections = ref<PasteCollection[]>([])
  const currentCollection = ref<CollectionWithPastes | undefined>(undefined)
  const loading = ref(false)

  async function fetchCollections(userId: string) {
    loading.value = true
    try {
      const response = await client.get(`/api/v2/collections/user/${userId}`)
      collections.value = response.data
    } catch (error) {
      console.error('Failed to fetch collections:', error)
      collections.value = []
    }
    loading.value = false
  }

  async function fetchCollection(collectionId: string) {
    loading.value = true
    try {
      const response = await client.get(`/api/v2/collections/${collectionId}`)
      currentCollection.value = response.data
    } catch (error) {
      console.error('Failed to fetch collection:', error)
      currentCollection.value = undefined
    }
    loading.value = false
  }

  async function createCollection(request: CollectionCreateRequest) {
    loading.value = true
    try {
      const response = await client.post('/api/v2/collections', request)
      collections.value.push(response.data)
      return response.data
    } catch (error) {
      console.error('Failed to create collection:', error)
      return null
    } finally {
      loading.value = false
    }
  }

  async function updateCollection(collectionId: string, request: CollectionUpdateRequest) {
    loading.value = true
    try {
      const response = await client.put(`/api/v2/collections/${collectionId}`, request)
      const index = collections.value.findIndex((c) => c.id === collectionId)
      if (index >= 0) {
        collections.value[index] = response.data
      }
      return response.data
    } catch (error) {
      console.error('Failed to update collection:', error)
      return null
    } finally {
      loading.value = false
    }
  }

  async function deleteCollection(collectionId: string) {
    loading.value = true
    try {
      await client.delete(`/api/v2/collections/${collectionId}`)
      collections.value = collections.value.filter((c) => c.id !== collectionId)
      return true
    } catch (error) {
      console.error('Failed to delete collection:', error)
      return false
    } finally {
      loading.value = false
    }
  }

  async function addPasteToCollection(collectionId: string, request: CollectionAddPasteRequest) {
    loading.value = true
    try {
      await client.post(`/api/v2/collections/${collectionId}/pastes`, request)
      if (currentCollection.value && currentCollection.value.collection.id === collectionId) {
        await fetchCollection(collectionId)
      }
      return true
    } catch (error) {
      console.error('Failed to add paste to collection:', error)
      return false
    } finally {
      loading.value = false
    }
  }

  async function removePasteFromCollection(collectionId: string, pasteKey: string) {
    loading.value = true
    try {
      await client.delete(`/api/v2/collections/${collectionId}/pastes/${pasteKey}`)
      if (currentCollection.value && currentCollection.value.collection.id === collectionId) {
        await fetchCollection(collectionId)
      }
      return true
    } catch (error) {
      console.error('Failed to remove paste from collection:', error)
      return false
    } finally {
      loading.value = false
    }
  }

  function reset() {
    collections.value = []
    currentCollection.value = undefined
  }

  return {
    collections,
    currentCollection,
    loading,
    fetchCollections,
    fetchCollection,
    createCollection,
    updateCollection,
    deleteCollection,
    addPasteToCollection,
    removePasteFromCollection,
    reset
  }
})
