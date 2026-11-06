export default defineEventHandler(async () => {
  return await api("/server/list");
});
