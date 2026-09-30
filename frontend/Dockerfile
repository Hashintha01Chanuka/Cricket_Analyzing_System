# --- Build stage ---
FROM node:20-alpine AS build
WORKDIR /app
COPY package.json package-lock.json* ./
RUN npm install
COPY . .

# The gateway URL is baked in at BUILD time, not runtime — Vite env vars
# (import.meta.env.VITE_*) are resolved when the bundle is built, not when
# the container starts. Overridable via --build-arg for different
# environments (local docker-compose vs. an AWS deployment where the
# gateway has a different host).
ARG VITE_GATEWAY_URL=http://localhost:8080
ENV VITE_GATEWAY_URL=$VITE_GATEWAY_URL
RUN npm run build

# --- Runtime stage ---
FROM nginx:1.27-alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
