import { z } from "zod";

const schema = z.array(z.uuid()).nonempty().max(500);

export default defineEventHandler(async (event) => {
  const result = await readValidatedBody(event, body => schema.safeParse(body));

  if (!result.success) {
    throw result.error.issues;
  }

  return await api(`/player/delete/bulk`, {
    body: result.data,
    method: "DELETE",
  });
});
