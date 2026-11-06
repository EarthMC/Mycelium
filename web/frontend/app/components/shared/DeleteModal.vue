<script setup lang="ts">
const props = defineProps<{
  name: string;
  toDelete: string[];
}>();

const emit = defineEmits(["delete"]);

const open = ref(false);

async function onSubmit() {
  await $fetch(`/api/${props.name}/delete/bulk`, {
    method: "DELETE",
    body: props.toDelete,
  });

  emit("delete");

  open.value = false;
}
</script>

<template>
  <UModal
    v-model:open="open"
    :title="`Delete ${toDelete.length} ${name}${toDelete.length > 1 ? 's' : ''}?`"
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
