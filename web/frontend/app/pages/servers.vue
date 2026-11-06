<script setup lang="ts">
import type { ServerResponse } from "~/types";
import type { TableColumn } from "@nuxt/ui";
import type { Row } from "@tanstack/table-core";

const UButton = resolveComponent("UButton");
const UDropdownMenu = resolveComponent("UDropdownMenu");
const UCheckbox = resolveComponent("UCheckbox");

const requestFetch = useRequestFetch();

const { data, refresh, status } = await useAsyncData("server-data", async (_nuxtApp, { signal }) => {
  return requestFetch<ServerResponse[]>("/api/server/list", { signal });
});

const toast = useToast();

function getRowItems(row: Row<ServerResponse>) {
  return [
    {
      type: "label",
      label: "Actions",
    },
    {
      label: "Delete server",
      icon: "i-lucide-trash",
      color: "error",
      async onSelect() {
        await $fetch(`/api/server/delete/${row.original.name}`, {
          method: "DELETE",
        });

        void refresh();

        toast.add({
          title: "Server deleted",
          description: "The server has been deleted.",
        });
      },
    },
  ];
}

const columns: TableColumn<ServerResponse>[] = [
  {
    id: "select",
    header: ({ table }) =>
      h(UCheckbox, {
        "modelValue": table.getIsSomePageRowsSelected()
          ? "indeterminate"
          : table.getIsAllPageRowsSelected(),
        "onUpdate:modelValue": (value: boolean | "indeterminate") =>
          table.toggleAllPageRowsSelected(!!value),
        "ariaLabel": "Select all",
      }),
    cell: ({ row }) =>
      h(UCheckbox, {
        "modelValue": row.getIsSelected(),
        "onUpdate:modelValue": (value: boolean | "indeterminate") => row.toggleSelected(!!value),
        "ariaLabel": "Select row",
      }),
  },
  {
    accessorKey: "name",
    header: "Name",
  },
  {
    accessorKey: "players",
    header: "Players",
  },
  {
    id: "actions",
    cell: ({ row }) => {
      return h(
        "div",
        { class: "text-right" },
        h(
          UDropdownMenu,
          {
            content: {
              align: "end",
            },
            items: getRowItems(row),
          },
          () =>
            h(UButton, {
              icon: "i-lucide-ellipsis-vertical",
              color: "neutral",
              variant: "ghost",
              class: "ml-auto",
            }),
        ),
      );
    },
  },
];
</script>

<template>
  <TablePage
    name="server"
    :columns="columns"
    :data="data || []"
    :status="status"
    :key-extractor="value => value.name"
    @update="refresh"
  />
</template>

<style scoped>

</style>
