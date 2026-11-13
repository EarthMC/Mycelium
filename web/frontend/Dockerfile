FROM node:26-alpine AS build

WORKDIR /app

COPY package*.json .
RUN npm ci

COPY . .

RUN npm run build

FROM node:26-alpine AS run

WORKDIR /app

COPY --from=build /app/.output .

EXPOSE 3000

ENTRYPOINT ["node", "server/index.mjs"]
