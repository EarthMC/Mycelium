export default defineEventHandler(async (event) => {
  return await api(`/server/delete/${getRouterParam(event, "server")}`, {
    method: "DELETE",
  });
});
