<script setup lang="ts">
import type { DropdownMenuItem } from "@nuxt/ui";

const items = [[{
  label: "New player",
  icon: "i-lucide-user-plus",
  to: "/players",
}, {
  label: "New server",
  icon: "i-lucide-user-plus",
  to: "/servers",
}, {
  label: "New proxy",
  icon: "i-lucide-user-plus",
  to: "/proxies",
}]] satisfies DropdownMenuItem[][];
</script>

<template>
  <UDashboardPanel id="home">
    <template #header>
      <UDashboardNavbar title="Home" :ui="{ right: 'gap-3' }">
        <template #leading>
          <UDashboardSidebarCollapse />
        </template>

        <template #right>
          <UDropdownMenu v-if="false" :items="items">
            <UButton icon="i-lucide-plus" size="md" class="rounded-full" />
          </UDropdownMenu>
        </template>
      </UDashboardNavbar>

      <UDashboardToolbar>
        <template #left>
          <!-- NOTE: The `-ms-1` class is used to align with the `DashboardSidebarCollapse` button here. -->
          <!--
          <HomeDateRangePicker v-model="range" class="-ms-1" />

          <HomePeriodSelect v-model="period" :range="range" />
          -->
        </template>
      </UDashboardToolbar>
    </template>

    <template #body>
      <HomeStats />
    </template>
  </UDashboardPanel>
</template>
