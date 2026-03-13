<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { client } from '@/utils/client'
import type { Branch } from '@/types/components'
import Card from 'primevue/card'
import Button from 'primevue/button'
import InputText from 'primevue/inputtext'
import Dialog from 'primevue/dialog'
import Select from 'primevue/select'
import Message from 'primevue/message'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import Tag from 'primevue/tag'

const props = defineProps<{
  pasteId: string
}>()

const emit = defineEmits<{
  branchCreated: [branch: Branch]
  branchMerged: [sourceBranch: string, targetBranch: string]
  branchDeleted: [branchName: string]
  branchSwitched: [branchName: string]
}>()

const branches = ref<Branch[]>([])
const currentBranch = ref<string>('main')
const isLoading = ref(false)
const showCreateDialog = ref(false)
const showMergeDialog = ref(false)
const showDeleteDialog = ref(false)
const selectedBranch = ref<Branch | null>(null)
const newBranchName = ref('')
const sourceBranch = ref<string>('')
const targetBranch = ref<string>('')
const error = ref<string | null>(null)

const branchOptions = computed(() =>
  branches.value.map(b => ({ label: b.name, value: b.name }))
)

const canDelete = computed(() =>
  selectedBranch.value && !selectedBranch.value.is_default
)

async function fetchBranches() {
  isLoading.value = true
  try {
    const response = await client.get(`/api/v2/pastes/${props.pasteId}/branches`)
    branches.value = response.data.branches
    currentBranch.value = response.data.current_branch
  } catch (e) {
    console.error('Failed to fetch branches:', e)
  } finally {
    isLoading.value = false
  }
}

async function createBranch() {
  if (!newBranchName.value.trim()) return

  isLoading.value = true
  error.value = null

  try {
    const response = await client.post(`/api/v2/pastes/${props.pasteId}/branches`, {
      name: newBranchName.value,
      source_branch: currentBranch.value
    })

    branches.value.push(response.data)
    emit('branchCreated', response.data)
    showCreateDialog.value = false
    newBranchName.value = ''
  } catch (e) {
    error.value = 'Failed to create branch'
    console.error('Branch creation error:', e)
  } finally {
    isLoading.value = false
  }
}

async function mergeBranch() {
  isLoading.value = true
  error.value = null

  try {
    await client.post(`/api/v2/pastes/${props.pasteId}/branches/merge`, {
      source: sourceBranch.value,
      target: targetBranch.value
    })

    emit('branchMerged', sourceBranch.value, targetBranch.value)
    showMergeDialog.value = false
    fetchBranches()
  } catch (e) {
    error.value = 'Failed to merge branches'
    console.error('Branch merge error:', e)
  } finally {
    isLoading.value = false
  }
}

async function deleteBranch() {
  if (!selectedBranch.value) return

  isLoading.value = true
  error.value = null

  try {
    await client.delete(
      `/api/v2/pastes/${props.pasteId}/branches/${selectedBranch.value.name}`
    )

    emit('branchDeleted', selectedBranch.value.name)
    showDeleteDialog.value = false
    fetchBranches()
  } catch (e) {
    error.value = 'Failed to delete branch'
    console.error('Branch deletion error:', e)
  } finally {
    isLoading.value = false
  }
}

async function switchBranch(branchName: string) {
  isLoading.value = true
  error.value = null

  try {
    await client.post(`/api/v2/pastes/${props.pasteId}/branches/switch`, {
      branch: branchName
    })

    currentBranch.value = branchName
    emit('branchSwitched', branchName)
  } catch (e) {
    error.value = 'Failed to switch branch'
    console.error('Branch switch error:', e)
  } finally {
    isLoading.value = false
  }
}

function openDeleteDialog(branch: Branch) {
  selectedBranch.value = branch
  showDeleteDialog.value = true
}

onMounted(() => {
  fetchBranches()
})
</script>

<template>
  <Card class="branch-manager">
    <template #title>
      <div class="flex items-center justify-between">
        <span>Branch Manager</span>
        <div class="flex gap-2">
          <Button
            label="New Branch"
            icon="pi pi-plus"
            size="small"
            @click="showCreateDialog = true"
          />
          <Button
            label="Merge"
            icon="pi pi-arrows-h"
            size="small"
            severity="secondary"
            @click="showMergeDialog = true"
          />
        </div>
      </div>
    </template>

    <template #content>
      <div class="space-y-4">
        <Message v-if="error" severity="error" :closable="true" @close="error = null">
          {{ error }}
        </Message>

        <DataTable
          :value="branches"
          :loading="isLoading"
          striped-rows
        >
          <Column field="name" header="Branch">
            <template #body="{ data }">
              <div class="flex items-center gap-2">
                <span class="font-medium">{{ data.name }}</span>
                <Tag
                  v-if="data.name === currentBranch"
                  severity="success"
                  value="Current"
                />
                <Tag
                  v-if="data.is_default"
                  severity="info"
                  value="Default"
                />
              </div>
            </template>
          </Column>

          <Column field="updated_at" header="Last Updated">
            <template #body="{ data }">
              {{ new Date(data.updated_at).toLocaleString() }}
            </template>
          </Column>

          <Column header="Actions">
            <template #body="{ data }">
              <div class="flex gap-2">
                <Button
                  v-if="data.name !== currentBranch"
                  label="Switch"
                  icon="pi pi-arrow-right"
                  size="small"
                  text
                  @click="switchBranch(data.name)"
                />
                <Button
                  v-if="!data.is_default"
                  label="Delete"
                  icon="pi pi-trash"
                  size="small"
                  severity="danger"
                  text
                  @click="openDeleteDialog(data)"
                />
              </div>
            </template>
          </Column>
        </DataTable>
      </div>
    </template>
  </Card>

  <Dialog
    v-model:visible="showCreateDialog"
    header="Create New Branch"
    :modal="true"
    :style="{ width: '450px' }"
  >
    <div class="space-y-4">
      <div>
        <label for="branch-name" class="mb-2 block text-sm font-medium">
          Branch Name
        </label>
        <InputText
          id="branch-name"
          v-model="newBranchName"
          placeholder="feature/my-feature"
          class="w-full"
          @keyup.enter="createBranch"
        />
      </div>
      <div class="text-sm text-gray-600 dark:text-gray-400">
        Will be created from: <span class="font-medium">{{ currentBranch }}</span>
      </div>
    </div>

    <template #footer>
      <Button
        label="Cancel"
        severity="secondary"
        outlined
        @click="showCreateDialog = false"
      />
      <Button
        label="Create"
        :loading="isLoading"
        :disabled="!newBranchName.trim()"
        @click="createBranch"
      />
    </template>
  </Dialog>

  <Dialog
    v-model:visible="showMergeDialog"
    header="Merge Branches"
    :modal="true"
    :style="{ width: '450px' }"
  >
    <div class="space-y-4">
      <div>
        <label for="source-branch" class="mb-2 block text-sm font-medium">
          Source Branch
        </label>
        <Select
          id="source-branch"
          v-model="sourceBranch"
          :options="branchOptions"
          option-label="label"
          option-value="value"
          placeholder="Select source"
          class="w-full"
        />
      </div>
      <div>
        <label for="target-branch" class="mb-2 block text-sm font-medium">
          Target Branch
        </label>
        <Select
          id="target-branch"
          v-model="targetBranch"
          :options="branchOptions"
          option-label="label"
          option-value="value"
          placeholder="Select target"
          class="w-full"
        />
      </div>
    </div>

    <template #footer>
      <Button
        label="Cancel"
        severity="secondary"
        outlined
        @click="showMergeDialog = false"
      />
      <Button
        label="Merge"
        :loading="isLoading"
        :disabled="!sourceBranch || !targetBranch || sourceBranch === targetBranch"
        @click="mergeBranch"
      />
    </template>
  </Dialog>

  <Dialog
    v-model:visible="showDeleteDialog"
    header="Delete Branch"
    :modal="true"
    :style="{ width: '450px' }"
  >
    <p>Are you sure you want to delete branch <strong>{{ selectedBranch?.name }}</strong>?</p>
    <p class="mt-2 text-sm text-gray-600 dark:text-gray-400">
      This action cannot be undone.
    </p>

    <template #footer>
      <Button
        label="Cancel"
        severity="secondary"
        outlined
        @click="showDeleteDialog = false"
      />
      <Button
        label="Delete"
        severity="danger"
        :loading="isLoading"
        :disabled="!canDelete"
        @click="deleteBranch"
      />
    </template>
  </Dialog>
</template>

<style scoped>
.branch-manager {
  width: 100%;
}
</style>
