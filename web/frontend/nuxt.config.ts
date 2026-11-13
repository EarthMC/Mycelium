// https://nuxt.com/docs/api/configuration/nuxt-config
export default defineNuxtConfig({
  modules: [
    "@nuxt/eslint",
    "@nuxt/ui",
    "@vueuse/nuxt",
    "@nuxt/image",
  ],

  devtools: {
    enabled: true,
  },

  css: ["~/assets/css/main.css"],

  runtimeConfig: {
    accessToken: "123", // Used to access the dashboard
    backendAuthToken: "changeme", // Used to authenticate with the backend
    backendUrl: "http://localhost:8098",
    secureCookie: true,
  },

  routeRules: {
    "/api/**": {
      cors: true,
    },
  },

  compatibilityDate: "2026-06-30",

  nitro: {
    imports: {
      dirs: ["./server/utils"],
    },
    experimental: {
      asyncContext: true,
    },
  },

  eslint: {
    config: {
      stylistic: {
        commaDangle: "never",
        braceStyle: "1tbs",
      },
    },
  },
});
