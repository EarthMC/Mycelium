<script setup lang="ts">
import { z } from "zod";
import type { FormSubmitEvent } from "@nuxt/ui";

const schema = z.object({
  name: z.string("Name is required"),
});

export type Schema = z.output<typeof schema>;

const props = defineProps<{
  type: string;
}>();

const emit = defineEmits(["update"]);

const open = ref(false);

const state = reactive<Partial<Schema>>({
  name: "",
});

const toast = useToast();
async function onSubmit(event: FormSubmitEvent<Schema>) {
  console.log("submitted");
  await $fetch(`/api/${props.type}/create/${event.data.name}`, {
    method: "POST",
  });

  emit("update");

  toast.add({ title: "Success", description: `Created ${props.type} ${event.data.name}`, color: "success" });
  open.value = false;
}
</script>

<template>
  <UModal v-model:open="open" :title="`New ${type}`" :description="`Add a new ${type} to the database`">
    <UButton :label="`New ${type}`" icon="i-lucide-plus" />

    <template #body>
      <UForm
        :schema="schema"
        :state="state"
        class="space-y-4"
        @submit="onSubmit"
      >
        <UFormField label="Name" name="name">
          <UInput
            v-model="state.name"
            class="w-full"
            required
            autofocus
          />
        </UFormField>
        <div class="flex justify-end gap-2">
          <UButton
            label="Cancel"
            color="neutral"
            variant="subtle"
            @click="open = false"
          />
          <UButton
            label="Create"
            color="primary"
            variant="solid"
            type="submit"
          />
        </div>
      </UForm>
    </template>
  </UModal>
</template>
