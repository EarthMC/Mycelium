export const api = $fetch.create({
  baseURL: useRuntimeConfig().backendUrl,

  onRequest({ options }) {
    const event = useEvent();
    if (!event) {
      throw createError({
        statusCode: 500,
        statusMessage: "Could not find current request context"
      })
    }

    // pass both the frontend server's token and the dashboard access token to the backend for validation
    // the auth middleware already redirects any unauthenticated requests, but this serves as an additional layer

    options.headers.set("Authorization", `Bearer ${useRuntimeConfig().backendAuthToken}`);

    const accessToken = getCookie(event, "mycelium-access-token");
    if (accessToken) {
      options.headers.set("X-Mycelium-Access", accessToken)
    }
  },
});
