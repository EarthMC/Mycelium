export default defineEventHandler(async (event) => {
  return await api(`/proxy/create/${getRouterParam(event, "proxy")}`, {
    method: "POST",
  });
});
