export type Collection = {
  id: string
  name: string
  description: string
  user_id: string
  is_public: boolean
  icon?: string
  color?: string
  created_at: string
  updated_at: string
  paste_count?: number
}

export type CreateCollectionRequest = {
  name: string
  description?: string
  isPublic?: boolean
  icon?: string
  color?: string
}

export type UpdateCollectionRequest = {
  name?: string
  description?: string
  isPublic?: boolean
  icon?: string
  color?: string
}

export type PasteCollection = Collection

export type CollectionWithPastes = {
  collection: Collection
  pastes: Array<{
    id: string
    key: string
    title: string
    created_at: string
  }>
}

export type CollectionCreateRequest = CreateCollectionRequest

export type CollectionUpdateRequest = UpdateCollectionRequest

export type CollectionAddPasteRequest = {
  paste_key: string
}
