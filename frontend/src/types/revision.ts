/**
 * Type definitions for Version Control System
 */

export interface PasteRevision {
  id: string
  pasteId: string
  userId: string
  branchId?: string
  commitMessage: string
  diffContent: string
  previousRevisionId?: string
  revisionNumber: number
  createdAt: string
  parentTitle: string
}

export interface PasteBranch {
  id: string
  pasteId: string
  userId: string
  branchName: string
  baseRevisionId?: string
  latestRevisionId?: string
  isDefault: boolean
  createdAt: string
  updatedAt: string
}

export interface RevisionCompareRequest {
  fromRevisionId: string
  toRevisionId: string
}

export interface RevisionCreateRequest {
  commitMessage: string
  diffContent: string
  branchId?: string
}

export interface BranchCreateRequest {
  branchName: string
  baseRevisionId?: string
}
