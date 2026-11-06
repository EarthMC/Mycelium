<script setup lang="ts">
import type { TableColumn } from "@nuxt/ui";
import { upperFirst } from "scule";
import { getPaginationRowModel } from "@tanstack/table-core";
import type { Row } from "@tanstack/table-core";
import type { PlayerResponse, ProxyListResponse, ServerResponse } from "~/types";
import { USelect } from "#components";

const UAvatar = resolveComponent("UAvatar");
const UButton = resolveComponent("UButton");
const UDropdownMenu = resolveComponent("UDropdownMenu");
const UCheckbox = resolveComponent("UCheckbox");

const toast = useToast();
const table = useTemplateRef("table");

const columnFilters = ref([{
  id: "name",
  value: "",
}]);

const columnVisibility = ref();
const rowSelection = ref({});

const unknownValue = "Unknown";

const requestFetch = useRequestFetch();

const { data, status, refresh } = await useAsyncData("players-data", async (_nuxtApp, { signal }) => {
  const [player, proxy, server] = await Promise.all([
    requestFetch<PlayerResponse[]>("/api/player/list", { signal }),
    requestFetch<ProxyListResponse>("/api/proxy/list", { signal }),
    requestFetch<ServerResponse[]>("/api/server/list", { signal }),
  ]);

  return { player, proxy, server };
});

const proxyData = computed(() => data.value?.proxy);
const serverData = computed(() => data.value?.server);

const editModalOpen = ref(false);
const selectedPlayer = ref<PlayerResponse | undefined>();
const addModalState: Ref<"add" | "update"> = ref("add");

const onlinePlayers: Ref<number> = computed(() => proxyData.value?.totalPlayers ?? 0);

function getRowItems(row: Row<PlayerResponse>) {
  return [
    {
      type: "label",
      label: "Actions",
    },
    {
      label: "Copy player UUID",
      icon: "i-lucide-copy",
      onSelect() {
        navigator.clipboard.writeText(row.original.uuid);
        toast.add({
          title: "Copied to clipboard",
          description: "Player UUID copied to clipboard",
        });
      },
    },
    {
      label: "Edit player",
      icon: "i-lucide-clipboard-edit",
      onSelect() {
        selectedPlayer.value = row.original;
        addModalState.value = "update";
        editModalOpen.value = true;
      },
    },
    {
      type: "separator",
    },
    {
      label: "Delete player",
      icon: "i-lucide-trash",
      color: "error",
      async onSelect() {
        await $fetch(`/api/player/delete/${row.original.uuid}`, {
          method: "DELETE",
        });

        void refresh();

        toast.add({
          title: "Player deleted",
          description: "The player has been deleted.",
        });
      },
    },
  ];
}

const columns: TableColumn<PlayerResponse>[] = [
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
    cell: ({ row }) => {
      return h("div", { class: "flex items-center gap-3" }, [
        h(UAvatar, {
          src: `https://crafthead.net/helm/${row.original.uuid}`,
          size: "lg",
        }),
        h("div", undefined, [
          h("p", { class: "font-medium text-highlighted" }, row.original.name),
          h("p", { class: "" }, `${row.original.uuid}`),
        ]),
      ]);
    },
  },
  {
    accessorKey: "server",
    header: () => h("div", { class: "flex items-center gap-1" }, [
      h("span", "Server"),
      h(USelect, {
        "modelValue": serverFilter.value,
        "items": [
          { label: "All", value: "all" },
          { type: "separator" },
          ...(serverData.value ?? []).map(server => ({ label: capitalize(server.name), value: server.name })),
          { type: "separator" },
          { label: unknownValue, value: unknownValue },
        ],
        "size": "xs",
        "class": "w-24",
        "onUpdate:modelValue": (value: string) => {
          serverFilter.value = value;
        },
      }),
    ]),
    cell: ({ row }) => row.original.server ?? unknownValue,
  },
  {
    accessorKey: "proxy",
    header: () => h("div", { class: "flex items-center gap-1" }, [
      h("span", "Proxy"),
      h(USelect, {
        "modelValue": proxyFilter.value,
        "items": [
          { label: "All", value: "all" },
          { type: "separator" },
          ...(proxyData.value?.proxies ?? []).map(proxy => ({ label: proxy.id, value: proxy.id })),
          { type: "separator" },
          { label: unknownValue, value: unknownValue },
        ],
        "size": "xs",
        "class": "w-24",
        "onUpdate:modelValue": (value: string) => {
          proxyFilter.value = value;
        },
      }),
    ]),
    cell: ({ row }) => row.original.proxy ?? unknownValue,
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

const serverFilter = ref("all");

watch(() => serverFilter.value, (newVal) => {
  if (!table?.value?.tableApi) return;

  const serverColumn = table.value.tableApi.getColumn("server");
  if (!serverColumn) return;

  if (newVal === "all") {
    serverColumn.setFilterValue(undefined);
  }
  else {
    serverColumn.setFilterValue(newVal);
  }
});

const proxyFilter = ref("all");

watch(() => proxyFilter.value, (newVal) => {
  if (!table?.value?.tableApi) return;

  const proxyColumn = table.value.tableApi.getColumn("proxy");
  if (!proxyColumn) return;

  if (newVal === "all") {
    proxyColumn.setFilterValue(undefined);
  }
  else {
    proxyColumn.setFilterValue(newVal);
  }
});

const name = computed({
  get: (): string => {
    return (table.value?.tableApi?.getColumn("name")?.getFilterValue() as string) || "";
  },
  set: (value: string) => {
    table.value?.tableApi?.getColumn("name")?.setFilterValue(value || undefined);
  },
});

const pagination = ref({
  pageIndex: 0,
  pageSize: 25,
});

function onDeleteRows() {
  void refresh();

  rowSelection.value = {};
}
</script>

<template>
  <UDashboardPanel id="players">
    <template #header>
      <UDashboardNavbar title="Players">
        <template #leading>
          <UDashboardSidebarCollapse />
        </template>

        <template #right>
          <PlayerAddModal
            v-model:open="editModalOpen"
            v-model:player="selectedPlayer"
            v-model:mode="addModalState"
            @update="refresh"
          />
        </template>
      </UDashboardNavbar>
    </template>

    <template #body>
      <p>There {{ onlinePlayers === 1 ? 'is' : 'are' }} currently <span class="text-primary">{{ onlinePlayers }}</span> player{{ onlinePlayers === 1 ? '' : 's' }} online.</p>

      <div class="flex flex-wrap items-center justify-between gap-1.5">
        <UInput
          v-model="name"
          class="max-w-sm"
          icon="i-lucide-search"
          placeholder="Filter names..."
        />

        <div class="flex flex-wrap items-center gap-1.5">
          <PlayerDeleteModal
            :to-delete="table?.tableApi?.getFilteredSelectedRowModel().rows.map(row => row.original) ?? []"
            @delete="onDeleteRows"
          >
            <UButton
              v-if="table?.tableApi?.getFilteredSelectedRowModel().rows.length"
              label="Delete"
              color="error"
              variant="subtle"
              icon="i-lucide-trash"
            >
              <template #trailing>
                <UKbd>
                  {{ table?.tableApi?.getFilteredSelectedRowModel().rows.length }}
                </UKbd>
              </template>
            </UButton>
          </PlayerDeleteModal>

          <UDropdownMenu
            :items="
              table?.tableApi
                ?.getAllColumns()
                .filter((column: any) => column.getCanHide())
                .map((column: any) => ({
                  label: upperFirst(column.id),
                  type: 'checkbox' as const,
                  checked: column.getIsVisible(),
                  onUpdateChecked(checked: boolean) {
                    table?.tableApi?.getColumn(column.id)?.toggleVisibility(checked)
                  },
                  onSelect(e?: Event) {
                    e?.preventDefault()
                  }
                }))
            "
            :content="{ align: 'end' }"
          >
            <UButton
              label="Display"
              color="neutral"
              variant="outline"
              trailing-icon="i-lucide-settings-2"
            />
          </UDropdownMenu>
        </div>
      </div>

      <UTable
        ref="table"
        v-model:column-filters="columnFilters"
        v-model:column-visibility="columnVisibility"
        v-model:row-selection="rowSelection"
        v-model:pagination="pagination"
        :pagination-options="{
          getPaginationRowModel: getPaginationRowModel()
        }"
        class="shrink-0"
        :data="data?.player"
        :columns="columns"
        :loading="status === 'pending'"
        :ui="{
          base: 'table-fixed border-separate border-spacing-0',
          thead: '[&>tr]:bg-elevated/50 [&>tr]:after:content-none',
          tbody: '[&>tr]:last:[&>td]:border-b-0',
          th: 'py-2 first:rounded-l-lg last:rounded-r-lg border-y border-default first:border-l last:border-r',
          td: 'border-b border-default',
          separator: 'h-0'
        }"
      />

      <div class="flex items-center justify-between gap-3 border-t border-default pt-4 mt-auto">
        <div class="text-sm text-muted">
          {{ table?.tableApi?.getFilteredSelectedRowModel().rows.length || 0 }} of
          {{ table?.tableApi?.getFilteredRowModel().rows.length || 0 }} row(s) selected.
        </div>

        <div class="flex items-center gap-1.5">
          <UPagination
            :default-page="(table?.tableApi?.getState().pagination.pageIndex || 0) + 1"
            :items-per-page="table?.tableApi?.getState().pagination.pageSize"
            :total="table?.tableApi?.getFilteredRowModel().rows.length"
            @update:page="(p: number) => table?.tableApi?.setPageIndex(p - 1)"
          />
        </div>
      </div>
    </template>
  </UDashboardPanel>
</template>
