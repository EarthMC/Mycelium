export default defineEventHandler((event) => {
  const url = getRequestURL(event);
  const path = url.pathname;

  if (path === "/api/auth/login") {
    return;
  }

  const token = getCookie(event, "access-token");
  const valid = token === useRuntimeConfig().accessToken;

  if (path === "/auth") {
    if (valid) {
      const redirect = getQuery(event).redirect;

      // don't allow external redirects
      const target
        = typeof redirect === "string"
          && redirect.startsWith("/")
          && !redirect.startsWith("//")
          ? redirect
          : "/";

      return sendRedirect(event, target);
    }

    return;
  }

  if (!valid) {
    const redirect = encodeURIComponent(url.pathname + url.search);
    return sendRedirect(event, `/auth?redirect=${redirect}`);
  }
});
