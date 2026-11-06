<script setup lang="ts">
import type { PlayerResponse } from "~/types";

const props = defineProps<{
  toDelete: PlayerResponse[];
}>();

const emit = defineEmits(["delete"]);

const open = ref(false);

async function onSubmit() {
  await $fetch("/api/player/delete/bulk", {
    body: props.toDelete.map(player => player.uuid),
    method: "DELETE",
  });

  emit("delete");

  open.value = false;
}
</script>

<template>
  <UModal
    v-model:open="open"
    :title="`Delete ${toDelete.length} player${toDelete.length > 1 ? 's' : ''}?`"
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
