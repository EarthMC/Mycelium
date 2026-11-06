<script setup lang="ts">
import { z } from "zod";
import type { FormSubmitEvent } from "@nuxt/ui";
import { redashUuid } from "~/utils";
import type { PlayerResponse, ProxyListResponse, ServerResponse } from "~/types";
import type { FetchError } from "ofetch";

const schema = z.object({
  name: z.string().min(3, "Must be at least 3 characters"),
  uuid: z.uuid("Invalid UUID"),
  proxy: z.string().optional(),
  server: z.string().optional(),
});

export type Schema = z.output<typeof schema>;

const open = defineModel<boolean>("open", { default: false });
const mode = defineModel<"add" | "update">("mode", { default: "add" });
const player = defineModel<PlayerResponse | undefined>("player");

const emit = defineEmits(["update"]);

const defaultState = () => ({
  name: "",
  uuid: "",
  proxy: undefined as string | undefined,
  server: undefined as string | undefined,
});

const state = reactive(defaultState());

const requestFetch = useRequestFetch();

const { data } = await useAsyncData("player-add-modal-data", async (_nuxtApp, { signal }) => {
  const [proxy, server] = await Promise.all([
    requestFetch<ProxyListResponse>("/api/proxy/list", { signal }),
    requestFetch<ServerResponse[]>("/api/server/list", { signal }),
  ]);

  return { proxy, server };
});

const proxies = computed(() => data.value?.proxy.proxies.map(proxy => proxy.id) ?? []);
const servers = computed(() => data.value?.server.map(server => server.name) ?? []);

watch(open, (isOpen) => {
  if (isOpen) {
    if (mode.value === "update" && player.value) {
      Object.assign(state, defaultState(), player.value);
    }
    else {
      Object.assign(state, defaultState());
    }
  }
});

const toast = useToast();
async function onSubmit(event: FormSubmitEvent<Schema>) {
  await $fetch("/api/player/create", {
    body: event.data,
    method: "PUT",
  });

  emit("update");

  toast.add({ title: "Success", description: `${mode.value === "add" ? "Added" : "Updated"} player ${event.data.name}`, color: "success" });
  open.value = false;
}

const uuidLoading = ref(false);

async function onUsernameInput() {
  if (!state.name || state.name.length === 0) {
    return;
  }

  try {
    uuidLoading.value = true;
    const result: { id: string; name: string } = await $fetch(`/api/mojangapi/player/lookup/${state.name}`);

    state.uuid = redashUuid(result.id);
    state.name = result.name;
  }
  catch (error) {
    const fetchError = error as FetchError;
    const message = fetchError.data?.errorMessage ?? fetchError.message;

    toast.add({
      title: "Could not find player",
      description: message,
      color: "error",
    });
  }
  finally {
    uuidLoading.value = false;
  }
}

function setModeToAdd() {
  mode.value = "add";
  Object.assign(state, defaultState());
}
</script>

<template>
  <UModal v-model:open="open" :title="mode === 'add' ? 'New player' : 'Update player'" :description="mode === 'add' ? 'Add a new player to the database' : 'Edit a player\'s data in the database'">
    <UButton label="New player" icon="i-lucide-plus" @click="setModeToAdd" />

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
            @change="onUsernameInput"
          />
        </UFormField>
        <UFormField label="UUID" name="uuid">
          <UInput
            v-model="state.uuid"
            class="w-full"
            required
            :loading="uuidLoading"
            :disabled="uuidLoading"
          />
        </UFormField>
        <UFormField label="Proxy" name="proxy">
          <USelect
            v-model="state.proxy"
            class="w-full"
            :items="proxies"
            placeholder="Select proxy"
          />
        </UFormField>
        <UFormField label="Server" name="server">
          <USelect
            v-model="state.server"
            class="w-full"
            :items="servers"
            placeholder="Select server"
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
            :label="mode === 'add' ? 'Create' : 'Update'"
            color="primary"
            variant="solid"
            type="submit"
          />
        </div>
      </UForm>
    </template>
  </UModal>
</template>
