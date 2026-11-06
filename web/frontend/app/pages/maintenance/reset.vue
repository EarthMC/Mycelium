<script setup lang="ts">

</script>

<template>
  <UPageCard title="Reset all data" description="Reset all Mycelium data in Redis, this cannot be undone.">
    <template #footer>
      <MaintenanceResetModal>
        <UButton label="Reset" color="error" />
      </MaintenanceResetModal>
    </template>
  </UPageCard>
</template>

<style scoped>

</style>
