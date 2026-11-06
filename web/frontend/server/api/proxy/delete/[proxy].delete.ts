export default defineEventHandler(async (event) => {
  return await api(`/proxy/delete/${getRouterParam(event, "proxy")}`, {
    method: "DELETE",
  });
});
