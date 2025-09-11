FROM node:20 AS builder
WORKDIR /app
COPY frontend/package*.json ./
RUN npm install
COPY frontend/ ./
RUN npm run build --prod

FROM nginx:alpine
COPY --from=builder /app/dist/frontend/browser/ /usr/share/nginx/html/
COPY frontend/nginx/nginx.conf /etc/nginx/nginx.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
