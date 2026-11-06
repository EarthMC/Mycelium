export default defineEventHandler(async (event) => {
  return await api(`/server/create/${getRouterParam(event, "server")}`, {
    method: "POST",
  });
});
