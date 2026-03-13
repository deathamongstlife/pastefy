<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { client } from '@/utils/client'
import { useCurrentUserStore } from '@/stores/current-user'
import type { Comment } from '@/types/components'
import Card from 'primevue/card'
import Avatar from 'primevue/avatar'
import Button from 'primevue/button'
import Textarea from 'primevue/textarea'

const props = defineProps<{
  pasteId: string
}>()

const emit = defineEmits<{
  commented: [comment: Comment]
}>()

const currentUser = useCurrentUserStore()
const comments = ref<Comment[]>([])
const newComment = ref('')
const replyTo = ref<string | null>(null)
const isLoading = ref(false)

const sortedComments = computed(() => {
  return comments.value.filter(c => !c.parent_id)
})

async function fetchComments() {
  isLoading.value = true
  try {
    const response = await client.get(`/api/v2/pastes/${props.pasteId}/comments`)
    comments.value = response.data.comments
  } catch (e) {
    console.error('Failed to fetch comments:', e)
  } finally {
    isLoading.value = false
  }
}

async function postComment() {
  if (!newComment.value.trim()) return

  isLoading.value = true
  try {
    const response = await client.post(`/api/v2/pastes/${props.pasteId}/comments`, {
      content: newComment.value,
      parent_id: replyTo.value
    })

    comments.value.push(response.data)
    emit('commented', response.data)
    newComment.value = ''
    replyTo.value = null
  } catch (e) {
    console.error('Failed to post comment:', e)
  } finally {
    isLoading.value = false
  }
}

async function likeComment(commentId: string) {
  try {
    await client.post(`/api/v2/comments/${commentId}/like`)
    const comment = findComment(commentId)
    if (comment) {
      comment.liked = !comment.liked
      comment.likes += comment.liked ? 1 : -1
    }
  } catch (e) {
    console.error('Failed to like comment:', e)
  }
}

function findComment(id: string, commentList: Comment[] = comments.value): Comment | null {
  for (const comment of commentList) {
    if (comment.id === id) return comment
    if (comment.replies.length > 0) {
      const found = findComment(id, comment.replies)
      if (found) return found
    }
  }
  return null
}

function formatTime(dateString: string): string {
  const date = new Date(dateString)
  const now = new Date()
  const diffMs = now.getTime() - date.getTime()
  const diffMins = Math.floor(diffMs / 60000)

  if (diffMins < 1) return 'Just now'
  if (diffMins < 60) return `${diffMins}m ago`
  if (diffMins < 1440) return `${Math.floor(diffMins / 60)}h ago`
  return date.toLocaleDateString()
}

onMounted(() => {
  fetchComments()
})
</script>

<template>
  <Card class="comment-thread">
    <template #title>
      <div class="flex items-center justify-between">
        <span>Comments ({{ comments.length }})</span>
        <Button
          icon="pi pi-refresh"
          text
          size="small"
          :loading="isLoading"
          @click="fetchComments"
        />
      </div>
    </template>

    <template #content>
      <div class="space-y-4">
        <div v-if="currentUser.isLoggedIn" class="new-comment">
          <div class="flex gap-3">
            <Avatar
              :image="currentUser.user?.avatar_url"
              :label="currentUser.user?.username[0]"
              shape="circle"
            />
            <div class="flex-1 space-y-2">
              <Textarea
                v-model="newComment"
                rows="3"
                placeholder="Write a comment..."
                class="w-full"
              />
              <div class="flex justify-end gap-2">
                <Button
                  v-if="replyTo"
                  label="Cancel"
                  size="small"
                  severity="secondary"
                  outlined
                  @click="replyTo = null"
                />
                <Button
                  label="Comment"
                  size="small"
                  :loading="isLoading"
                  :disabled="!newComment.trim()"
                  @click="postComment"
                />
              </div>
            </div>
          </div>
        </div>

        <div class="comments-list space-y-4">
          <CommentItem
            v-for="comment in sortedComments"
            :key="comment.id"
            :comment="comment"
            :current-user-id="currentUser.user?.id"
            @like="likeComment"
            @reply="replyTo = $event"
          />
        </div>

        <div v-if="comments.length === 0" class="py-8 text-center text-gray-500">
          <i class="pi pi-comments mb-2 text-4xl"></i>
          <p>No comments yet. Be the first to comment!</p>
        </div>
      </div>
    </template>
  </Card>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'

export const CommentItem = defineComponent({
  name: 'CommentItem',
  props: {
    comment: {
      type: Object as PropType<Comment>,
      required: true
    },
    currentUserId: String
  },
  emits: ['like', 'reply'],
  template: `
    <div class="comment-item">
      <div class="flex gap-3">
        <Avatar
          :image="comment.user.avatar_url"
          :label="comment.user.username[0]"
          shape="circle"
        />
        <div class="flex-1">
          <div class="comment-content rounded bg-gray-50 p-3 dark:bg-gray-800">
            <div class="mb-1 font-semibold">{{ comment.user.username }}</div>
            <p class="text-sm">{{ comment.content }}</p>
          </div>
          <div class="mt-2 flex items-center gap-4 text-sm text-gray-600 dark:text-gray-400">
            <span>{{ formatTime(comment.created_at) }}</span>
            <button
              @click="$emit('like', comment.id)"
              :class="['flex items-center gap-1 hover:text-blue-500', comment.liked ? 'text-blue-500' : '']"
            >
              <i :class="['pi', comment.liked ? 'pi-heart-fill' : 'pi-heart']"></i>
              {{ comment.likes }}
            </button>
            <button
              @click="$emit('reply', comment.id)"
              class="hover:text-blue-500"
            >
              Reply
            </button>
          </div>
          <div v-if="comment.replies.length > 0" class="ml-8 mt-3 space-y-3">
            <CommentItem
              v-for="reply in comment.replies"
              :key="reply.id"
              :comment="reply"
              :current-user-id="currentUserId"
              @like="$emit('like', $event)"
              @reply="$emit('reply', $event)"
            />
          </div>
        </div>
      </div>
    </div>
  `
})
</script>

<style scoped>
.comment-thread {
  width: 100%;
}

.new-comment {
  padding-bottom: 1rem;
  border-bottom: 1px solid var(--surface-border);
}
</style>
