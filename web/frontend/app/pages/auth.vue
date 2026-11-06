<script setup lang="ts">
import type { AuthFormField, FormSubmitEvent } from "@nuxt/ui";
import { z } from "zod";
import type { FetchError } from "ofetch";

const toast = useToast();

const fields: AuthFormField[] = [{
  name: "token",
  type: "password",
  label: "Token",
  placeholder: "Enter your token",
  required: true,
}];

const schema = z.object({
  token: z.string("Token is required"),
});
type Schema = z.output<typeof schema>;

async function onSubmit(payload: FormSubmitEvent<Schema>) {
  try {
    const response = await $fetch("/api/auth/login", {
      method: "POST",
      body: payload.data,
    });

    if (response.success) {
      navigateTo("/");
    }
  }
  catch (error) {
    const fetchError = error as FetchError;
    const message = fetchError.data?.statusMessage ?? fetchError.message;

    toast.add({
      title: "Error",
      description: message,
      color: "error",
    });
  }
}

definePageMeta({
  layout: false,
});
</script>

<template>
  <UPage>
    <UPageSection>
      <UAuthForm
        :schema="schema"
        title="Login"
        description="Enter your credentials to access the dashboard."
        icon="i-lucide-user"
        :fields="fields"
        @submit="onSubmit"
      />
    </UPageSection>
  </UPage>
</template>

<style scoped>

</style>
