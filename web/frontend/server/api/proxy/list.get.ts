export default defineEventHandler(async () => {
  return await api("/proxy/list");
});
