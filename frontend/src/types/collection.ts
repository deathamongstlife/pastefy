/**
 * Type definitions for Enhanced Organization
 */

export interface PasteCollection {
  id: string
  userId: string
  name: string
  description?: string
  isPublic: boolean
  coverImage?: string
  createdAt: string
  updatedAt: string
}

export interface CollectionPaste {
  id: string
  collectionId: string
  pasteId: string
  sortOrder: number
  addedAt: string
}

export interface CollectionWithPastes {
  collection: PasteCollection
  pastes: any[]
}

export interface CollectionCreateRequest {
  name: string
  description?: string
  isPublic?: boolean
}

export interface CollectionUpdateRequest {
  name?: string
  description?: string
  isPublic?: boolean
}

export interface CollectionAddPasteRequest {
  pasteKey: string
  sortOrder?: number
}
