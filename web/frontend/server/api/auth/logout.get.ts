export default defineEventHandler(async (event) => {
  if (getCookie(event, "access-token") === undefined) {
    throw createError({
      statusCode: 400,
      message: "You are not logged in",
    });
  }

  deleteCookie(event, "access-token");

  return { success: true };
});
