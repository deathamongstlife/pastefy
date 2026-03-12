export type BulkPasteRequest = {
  pasteKeys: string[]
  action: 'delete' | 'move' | 'visibility'
  folderId?: string
  visibility?: string
}

export type BulkFolderRequest = {
  folderKeys: string[]
  action: 'delete' | 'move'
  parentFolderId?: string
}

export type BulkOperationResponse = {
  successful: string[]
  failed: string[]
  total_processed: number
  success_count: number
  failed_count: number
}
