export default defineEventHandler(async () => {
  return await api("/player/list");
});
