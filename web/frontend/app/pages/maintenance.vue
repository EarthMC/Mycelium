<script setup lang="ts">
import type { NavigationMenuItem } from "@nuxt/ui";

const links = [[{
  label: "Reset",
  icon: "i-lucide-user",
  to: "/maintenance/reset",
}]] satisfies NavigationMenuItem[][];
</script>

<template>
  <UDashboardPanel id="maintenance" :ui="{ body: 'lg:py-12' }">
    <template #header>
      <UDashboardNavbar title="Maintenance">
        <template #leading>
          <UDashboardSidebarCollapse />
        </template>
      </UDashboardNavbar>

      <UDashboardToolbar>
        <!-- NOTE: The `-mx-1` class is used to align with the `DashboardSidebarCollapse` button here. -->
        <UNavigationMenu :items="links" highlight class="-mx-1 flex-1" />
      </UDashboardToolbar>
    </template>

    <template #body>
      <div class="flex flex-col gap-4 sm:gap-6 lg:gap-12 w-full lg:max-w-2xl mx-auto">
        <NuxtPage />
      </div>
    </template>
  </UDashboardPanel>
</template>
