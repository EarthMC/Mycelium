export default defineEventHandler(async (event) => {
  const body = await readBody(event);

  const token = body.token;

  if (token !== useRuntimeConfig().accessToken) {
    throw createError({
      statusCode: 401,
      statusMessage: "Invalid token",
    });
  }

  setCookie(event, "mycelium-access-token", token, {
    httpOnly: true,
    sameSite: "strict",
    secure: true,
    path: "/",
  });

  return { success: true };
});
