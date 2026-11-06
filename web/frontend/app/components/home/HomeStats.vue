<script setup lang="ts">
import type { ProxyListResponse, ServerResponse, Stat } from "~/types";

const requestFetch = useRequestFetch();

const { data: data } = await useAsyncData("data", async (_nuxtApp, { signal }) => {
  const [proxy, server] = await Promise.all([
    requestFetch<ProxyListResponse>("/api/proxy/list", { signal }),
    requestFetch<ServerResponse[]>("/api/server/list", { signal }),
  ]);

  return { proxy, server };
});

const stats: Ref<Stat[]> = computed<Stat[]>(() => {
  const baseStats = [
    {
      title: "Online Players",
      to: "/players",
      icon: "i-lucide-users",
      value: data.value?.proxy?.totalPlayers,
    },
    {
      title: "Servers",
      to: "/servers",
      icon: "i-lucide-server",
      value: data.value?.server?.length,
    },
    {
      title: "Proxies",
      to: "/proxies",
      icon: "i-lucide-server-nas",
      value: data.value?.proxy?.proxies?.length,
    },
  ];

  return baseStats.map(stat => ({
    ...stat,
    value: stat.value ?? 0,
    success: stat.value !== undefined,
  }));
});
</script>

<template>
  <UPageGrid class="lg:grid-cols-4 gap-4 sm:gap-6 lg:gap-px">
    <UPageCard
      v-for="(stat, index) in stats"
      :key="index"
      :icon="stat.icon"
      :title="stat.title"
      :to="stat.to || ''"
      variant="subtle"
      :ui="{
        container: 'gap-y-1.5',
        wrapper: 'items-start',
        leading: 'p-2.5 rounded-full bg-primary/10 ring ring-inset ring-primary/25 flex-col',
        title: 'font-normal text-muted text-xs uppercase'
      }"
      class="lg:rounded-none first:rounded-l-lg last:rounded-r-lg hover:z-1"
    >
      <div class="flex items-center gap-2">
        <span class="text-2xl font-semibold text-highlighted">
          {{ stat.value }}
        </span>

        <UBadge
          :color="stat.success ? 'success' : 'error'"
          variant="subtle"
          class="text-xs"
        >
          {{ !stat.success ? 'Error' : '' }}
        </UBadge>
      </div>
    </UPageCard>
  </UPageGrid>
</template>
