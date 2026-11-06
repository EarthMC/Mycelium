import { z } from "zod";

const schema = z.array(z.string()).nonempty().max(100);

export default defineEventHandler(async (event) => {
  const result = await readValidatedBody(event, body => schema.safeParse(body));

  if (!result.success) {
    throw result.error.issues;
  }

  return await api(`/proxy/delete/bulk`, {
    body: result.data,
    method: "DELETE",
  });
});
