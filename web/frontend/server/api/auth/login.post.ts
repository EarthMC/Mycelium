export default defineEventHandler(async (event) => {
  const body = await readBody(event);

  const token = body.token;

  if (token !== useRuntimeConfig().accessToken) {
    throw createError({
      statusCode: 401,
      statusMessage: "Invalid token",
    });
  }

  const secure = useRuntimeConfig().secureCookie || true;

  setCookie(event, "mycelium-access-token", token, {
    httpOnly: true,
    sameSite: "strict",
    secure,
    path: "/",
  });

  return { success: true };
});
