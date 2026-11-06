<script setup lang="ts">
import type { NavigationMenuItem } from "@nuxt/ui";

const route = useRoute();

const open = ref(false);

const links = [[{
  label: "Home",
  icon: "i-lucide-house",
  to: "/",
  onSelect: () => {
    open.value = false;
  },
}, {
  label: "Players",
  icon: "i-lucide-users",
  to: "/players",
  onSelect: () => {
    open.value = false;
  },
}, {
  label: "Servers",
  icon: "i-lucide-server",
  to: "/servers",
  onSelect: () => {
    open.value = false;
  },
}, {
  label: "Proxies",
  icon: "i-lucide-server-nas",
  to: "/proxies",
  onSelect: () => {
    open.value = false;
  },
}, {
  label: "Maintenance Tools",
  to: "/maintenance",
  icon: "i-lucide-database-cog",
  defaultOpen: true,
  type: "trigger",
  children: [{
    label: "Reset",
    to: "/maintenance/reset",
    exact: true,
    onSelect: () => {
      open.value = false;
    },
  }],
}]] satisfies NavigationMenuItem[][];

const groups = computed(() => [{
  id: "links",
  label: "Go to",
  items: links.flat(),
}, {
  id: "code",
  label: "Code",
  items: [{
    id: "source",
    label: "View page source",
    icon: "i-simple-icons-github",
    to: `https://github.com/nuxt-ui-templates/dashboard/blob/main/app/pages${route.path === "/" ? "/index" : route.path}.vue`,
    target: "_blank",
  }],
}]);
</script>

<template>
  <UDashboardGroup unit="rem">
    <UDashboardSidebar
      id="default"
      v-model:open="open"
      collapsible
      resizable
      class="bg-elevated/25"
      :ui="{ footer: 'lg:border-t lg:border-default' }"
    >
      <template #header="{ collapsed }">
        <NuxtLink to="/">
          <NuxtImg src="/mycelium.png" width="48" />
        </NuxtLink>
        <p v-if="!collapsed">
          Mycelium
        </p>
      </template>

      <template #default="{ collapsed }">
        <UDashboardSearchButton :collapsed="collapsed" class="bg-transparent ring-default" />

        <UNavigationMenu
          :collapsed="collapsed"
          :items="links[0]"
          orientation="vertical"
          tooltip
          popover
        />

        <UNavigationMenu
          :collapsed="collapsed"
          :items="links[1]"
          orientation="vertical"
          tooltip
          class="mt-auto"
        />
      </template>

      <template #footer="{ collapsed }">
        <UserMenu :collapsed="collapsed" />
      </template>
    </UDashboardSidebar>

    <UDashboardSearch :groups="groups" />

    <slot />
  </UDashboardGroup>
</template>
