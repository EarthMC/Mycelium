export default defineEventHandler(async () => {
  return await api("/maintenance/reset");
});
