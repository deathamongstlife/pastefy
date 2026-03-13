export type AIStatusResponse = {
  enabled: boolean
  gateway_connected: boolean
  agent_id: string | null
  message: string
}

export type AITextResponse = {
  result: string
  success: boolean
}

export type BugInfo = {
  line?: number
  severity: 'low' | 'medium' | 'high' | 'critical'
  description: string
  suggestion: string
}

export type BugDetectionResponse = {
  bugs: BugInfo[]
  has_critical_issues: boolean
  summary: string
}

export type TagGenerationResponse = {
  tags: string[]
}

export type CodeTranslationResponse = {
  result: string
  success: boolean
}

export type QualityAnalysisResponse = {
  overall_score: number
  readability: number
  maintainability: number
  complexity: 'low' | 'medium' | 'high'
  best_practices: number
  issues: string[]
  strengths: string[]
  summary: string
}

export type CodeImprovement = {
  category: string
  description: string
  priority: 'low' | 'medium' | 'high'
  code_snippet: string
}

export type ImprovementSuggestionsResponse = {
  improvements: CodeImprovement[]
  summary: string
}

export type AIRequest = {
  code: string
  language?: string
}

export type TranslateCodeRequest = {
  code: string
  fromLanguage: string
  toLanguage: string
}

export type GenerateDocsRequest = {
  code: string
  language?: string
  format?: 'markdown' | 'jsdoc' | 'javadoc' | 'html'
}

export type GenerateTagsRequest = {
  title?: string
  content: string
  language?: string
}
