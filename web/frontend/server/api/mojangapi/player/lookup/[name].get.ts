export default defineEventHandler(async (event) => {
  const name = getRouterParam(event, "name");

  if (!name) {
    throw createError({
      status: 400,
      statusMessage: "No name provided",
    });
  }

  return await $fetch(`https://api.mojang.com/minecraft/profile/lookup/name/${name}`);
});
