import { z } from "zod";

const schema = z.object({
  name: z.string().min(3, "Too short"),
  uuid: z.uuid("Invalid UUID"),
  proxy: z.string().optional(),
  server: z.string().optional(),
});

export default defineEventHandler(async (event) => {
  const result = await readValidatedBody(event, body => schema.safeParse(body));

  if (!result.success) {
    throw result.error.issues;
  }

  return await api("/player/create", {
    method: "PUT",
    body: result.data,
  });
});
