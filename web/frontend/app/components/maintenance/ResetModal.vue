<script setup lang="ts">
const open = ref(false);

async function onSubmit() {
  await $fetch("/api/maintenance/reset");
  open.value = false;
}
</script>

<template>
  <UModal
    v-model:open="open"
    :title="`Reset the Redis database?`"
    :description="`Are you sure, this action cannot be undone.`"
  >
    <slot />

    <template #body>
      <div class="flex justify-end gap-2">
        <UButton
          label="Cancel"
          color="neutral"
          variant="subtle"
          @click="open = false"
        />
        <UButton
          label="Delete"
          color="error"
          variant="solid"
          loading-auto
          @click="onSubmit"
        />
      </div>
    </template>
  </UModal>
</template>
