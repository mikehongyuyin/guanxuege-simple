// 是否使用本地 mock 数据。
// true  → 不依赖后端，界面开箱即用（默认）
// false → 通过网关访问真实微服务。
// 容器/Nginx 构建时由 VITE_USE_MOCK=false 关闭 mock（见 frontend/Dockerfile）。
export const USE_MOCK = import.meta.env.VITE_USE_MOCK !== 'false'

export const API_BASE = '/api'
