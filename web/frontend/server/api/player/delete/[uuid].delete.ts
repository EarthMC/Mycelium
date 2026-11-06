export default defineEventHandler(async (event) => {
  const uuid = getRouterParam(event, "uuid");

  if (!uuid || !/^[0-9a-f]{8}-[0-9a-f]{4}-[0-5][0-9a-f]{3}-[089ab][0-9a-f]{3}-[0-9a-f]{12}$/i.test(uuid)) {
    throw createError({
      statusCode: 400,
      statusMessage: "Invalid UUID provided",
    });
  }

  return await api(`/player/delete/${uuid}`, {
    method: "DELETE",
  });
});
