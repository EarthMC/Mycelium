<script setup lang="ts">
import type { ProxyListResponse, ProxyResponse } from "~/types";
import type { TableColumn } from "@nuxt/ui";
import type { Row } from "@tanstack/table-core";

const UButton = resolveComponent("UButton");
const UDropdownMenu = resolveComponent("UDropdownMenu");
const UCheckbox = resolveComponent("UCheckbox");

const requestFetch = useRequestFetch();

const { data, refresh, status } = await useAsyncData("proxy-data", async (_nuxtApp, { signal }) => {
  return requestFetch<ProxyListResponse>("/api/proxy/list", { signal });
});

const toast = useToast();

function getRowItems(row: Row<ProxyResponse>) {
  return [
    {
      type: "label",
      label: "Actions",
    },
    {
      label: "Delete proxy",
      icon: "i-lucide-trash",
      color: "error",
      async onSelect() {
        await $fetch(`/api/proxy/delete/${row.original.id}`, {
          method: "DELETE",
        });

        void refresh();

        toast.add({
          title: "Proxy deleted",
          description: "The proxy has been deleted.",
        });
      },
    },
  ];
}

const columns: TableColumn<ProxyResponse>[] = [
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
    accessorKey: "id",
    header: "Id",
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
    name="proxy"
    :columns="columns"
    :data="data?.proxies ?? []"
    :status="status"
    :key-extractor="value => value.id"
    @update="refresh"
  />
</template>

<style scoped>

</style>
