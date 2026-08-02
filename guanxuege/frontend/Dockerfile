# 前端：Node 构建 → Nginx 托管静态资源并反向代理 /api 到网关

FROM node:20-alpine AS build
WORKDIR /app
COPY package.json package-lock.json* ./
RUN npm install
COPY . .
# 容器化时关闭 mock，走网关访问真实微服务
ENV VITE_USE_MOCK=false
RUN npm run build

FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
