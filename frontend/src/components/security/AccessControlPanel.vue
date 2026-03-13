<script setup lang="ts">
import { ref, computed } from 'vue'
import { client } from '@/utils/client'
import Card from 'primevue/card'
import InputText from 'primevue/inputtext'
import Password from 'primevue/password'
import Textarea from 'primevue/textarea'
import InputNumber from 'primevue/inputnumber'
import Calendar from 'primevue/datepicker'
import Checkbox from 'primevue/checkbox'
import Button from 'primevue/button'
import Message from 'primevue/message'

type AccessSettings = {
  password_protected: boolean
  password?: string
  ip_whitelist: string[]
  ip_blacklist: string[]
  max_views?: number
  expire_at?: string
}

const props = defineProps<{
  pasteId: string
  initialSettings?: AccessSettings
}>()

const emit = defineEmits<{
  saved: [settings: AccessSettings]
}>()

const passwordEnabled = ref(props.initialSettings?.password_protected ?? false)
const password = ref(props.initialSettings?.password ?? '')
const ipWhitelist = ref(props.initialSettings?.ip_whitelist?.join('\n') ?? '')
const ipBlacklist = ref(props.initialSettings?.ip_blacklist?.join('\n') ?? '')
const maxViews = ref(props.initialSettings?.max_views ?? null)
const expirationEnabled = ref(!!props.initialSettings?.expire_at)
const expirationDate = ref<Date | null>(
  props.initialSettings?.expire_at ? new Date(props.initialSettings.expire_at) : null
)

const isLoading = ref(false)
const error = ref<string | null>(null)
const successMessage = ref<string | null>(null)

const passwordStrength = computed(() => {
  if (!password.value) return 0
  let strength = 0
  if (password.value.length >= 8) strength++
  if (/[a-z]/.test(password.value)) strength++
  if (/[A-Z]/.test(password.value)) strength++
  if (/[0-9]/.test(password.value)) strength++
  if (/[^a-zA-Z0-9]/.test(password.value)) strength++
  return (strength / 5) * 100
})

const passwordStrengthLabel = computed(() => {
  const strength = passwordStrength.value
  if (strength < 40) return 'Weak'
  if (strength < 70) return 'Medium'
  return 'Strong'
})

const passwordStrengthColor = computed(() => {
  const strength = passwordStrength.value
  if (strength < 40) return 'text-red-600 dark:text-red-400'
  if (strength < 70) return 'text-yellow-600 dark:text-yellow-400'
  return 'text-green-600 dark:text-green-400'
})

async function saveSettings() {
  isLoading.value = true
  error.value = null
  successMessage.value = null

  try {
    const settings: AccessSettings = {
      password_protected: passwordEnabled.value,
      password: passwordEnabled.value ? password.value : undefined,
      ip_whitelist: ipWhitelist.value
        .split('\n')
        .map(ip => ip.trim())
        .filter(ip => ip.length > 0),
      ip_blacklist: ipBlacklist.value
        .split('\n')
        .map(ip => ip.trim())
        .filter(ip => ip.length > 0),
      max_views: maxViews.value ?? undefined,
      expire_at: expirationEnabled.value && expirationDate.value
        ? expirationDate.value.toISOString()
        : undefined
    }

    await client.put(`/api/v2/pastes/${props.pasteId}/access`, settings)

    successMessage.value = 'Access control settings saved successfully'
    emit('saved', settings)
  } catch (e) {
    error.value = 'Failed to save access control settings'
    console.error('Access control save error:', e)
  } finally {
    isLoading.value = false
  }
}
</script>

<template>
  <Card class="access-control-panel">
    <template #title>
      <div class="flex items-center gap-2">
        <i class="pi pi-shield text-blue-500"></i>
        <span>Access Control</span>
      </div>
    </template>

    <template #content>
      <div class="space-y-6">
        <Message v-if="error" severity="error" :closable="true" @close="error = null">
          {{ error }}
        </Message>

        <Message v-if="successMessage" severity="success" :closable="true" @close="successMessage = null">
          {{ successMessage }}
        </Message>

        <div class="password-section">
          <div class="mb-3 flex items-center gap-2">
            <Checkbox
              v-model="passwordEnabled"
              input-id="password-toggle"
              :binary="true"
            />
            <label for="password-toggle" class="font-semibold">
              Password Protection
            </label>
          </div>

          <div v-if="passwordEnabled" class="space-y-3">
            <div>
              <label for="password-input" class="mb-2 block text-sm font-medium">
                Password
              </label>
              <Password
                id="password-input"
                v-model="password"
                toggle-mask
                :feedback="false"
                placeholder="Enter password"
                class="w-full"
              />
            </div>

            <div v-if="password" class="password-strength">
              <div class="mb-1 flex items-center justify-between text-sm">
                <span class="text-gray-600 dark:text-gray-400">Strength:</span>
                <span :class="passwordStrengthColor">{{ passwordStrengthLabel }}</span>
              </div>
              <div class="h-2 w-full overflow-hidden rounded bg-gray-200 dark:bg-gray-700">
                <div
                  class="h-full transition-all"
                  :class="
                    passwordStrength < 40 ? 'bg-red-500' :
                    passwordStrength < 70 ? 'bg-yellow-500' : 'bg-green-500'
                  "
                  :style="{ width: `${passwordStrength}%` }"
                ></div>
              </div>
            </div>
          </div>
        </div>

        <div class="ip-whitelist-section">
          <label for="ip-whitelist" class="mb-2 block font-semibold">
            IP Whitelist
          </label>
          <Textarea
            id="ip-whitelist"
            v-model="ipWhitelist"
            rows="4"
            placeholder="192.168.1.1&#10;10.0.0.0/24&#10;One IP per line"
            class="font-mono text-sm"
          />
          <div class="mt-1 text-xs text-gray-500">
            Only these IPs can access. Leave empty to allow all.
          </div>
        </div>

        <div class="ip-blacklist-section">
          <label for="ip-blacklist" class="mb-2 block font-semibold">
            IP Blacklist
          </label>
          <Textarea
            id="ip-blacklist"
            v-model="ipBlacklist"
            rows="4"
            placeholder="192.168.1.100&#10;10.0.0.0/24&#10;One IP per line"
            class="font-mono text-sm"
          />
          <div class="mt-1 text-xs text-gray-500">
            These IPs will be blocked from access.
          </div>
        </div>

        <div class="max-views-section">
          <label for="max-views" class="mb-2 block font-semibold">
            Maximum Views
          </label>
          <InputNumber
            id="max-views"
            v-model="maxViews"
            :min="0"
            placeholder="Unlimited"
            show-buttons
            class="w-full"
          />
          <div class="mt-1 text-xs text-gray-500">
            Leave empty for unlimited views.
          </div>
        </div>

        <div class="expiration-section">
          <div class="mb-3 flex items-center gap-2">
            <Checkbox
              v-model="expirationEnabled"
              input-id="expiration-toggle"
              :binary="true"
            />
            <label for="expiration-toggle" class="font-semibold">
              Auto-Expiration
            </label>
          </div>

          <div v-if="expirationEnabled">
            <label for="expiration-date" class="mb-2 block text-sm font-medium">
              Expiration Date & Time
            </label>
            <Calendar
              id="expiration-date"
              v-model="expirationDate"
              show-time
              show-icon
              show-button-bar
              :min-date="new Date()"
              placeholder="Select date and time"
              class="w-full"
            />
          </div>
        </div>

        <Button
          label="Save Settings"
          icon="pi pi-save"
          :loading="isLoading"
          @click="saveSettings"
          class="w-full"
        />
      </div>
    </template>
  </Card>
</template>

<style scoped>
.access-control-panel {
  width: 100%;
}

.password-section,
.ip-whitelist-section,
.ip-blacklist-section,
.max-views-section,
.expiration-section {
  padding-bottom: 1.5rem;
  border-bottom: 1px solid var(--surface-border);
}

.expiration-section {
  border-bottom: none;
}
</style>
