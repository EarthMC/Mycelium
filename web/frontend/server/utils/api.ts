export const api = $fetch.create({
  baseURL: useRuntimeConfig().backendUrl,

  onRequest({ options }) {
    options.headers.set("Authorization", `Bearer ${useRuntimeConfig().backendAuthToken}`);
  },
});
