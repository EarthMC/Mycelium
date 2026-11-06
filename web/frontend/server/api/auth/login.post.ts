export default defineEventHandler(async (event) => {
  const body = await readBody(event);

  const token = body.token;

  if (token !== useRuntimeConfig().accessToken) {
    throw createError({
      statusCode: 401,
      statusMessage: "Invalid token",
    });
  }

  setCookie(event, "access-token", token, {
    httpOnly: true,
    sameSite: "lax",
    path: "/",
  });

  return { success: true };
});
