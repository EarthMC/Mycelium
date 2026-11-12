export default defineEventHandler(async (event) => {
  if (getCookie(event, "mycelium-access-token") === undefined) {
    throw createError({
      statusCode: 400,
      message: "You are not logged in",
    });
  }

  deleteCookie(event, "mycelium-access-token");

  return { success: true };
});
