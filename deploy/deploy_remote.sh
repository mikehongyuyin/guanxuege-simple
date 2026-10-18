#!/usr/bin/env bash
# ============================================================
# 观学阁 云服务器一键部署脚本
# 用法（在 /opt/guanxuege 目录下执行）：
#   bash deploy/deploy_remote.sh
# 前提：
#   1. 已安装 Docker 与 docker compose 插件
#   2. 已通过 upload_to_server.ps1 上传镜像(gx-images.tar)与项目文件
# ============================================================
set -e
cd "$(dirname "$0")/.."   # 切换到项目根目录

echo "== [1/5] 检查 Docker 环境 =="
if ! command -v docker >/dev/null 2>&1; then
  echo "错误：未检测到 docker，请先安装（见部署指南阶段二）"
  exit 1
fi
docker compose version >/dev/null 2>&1 || {
  echo "错误：缺少 docker compose 插件（dnf install -y docker-compose-plugin）"
  exit 1
}
docker info >/dev/null 2>&1 || {
  echo "错误：docker 服务未运行（systemctl start docker）"
  exit 1
}

echo "== [2/5] 加载本地导出的镜像（若存在） =="
if [ -f gx-images.tar ]; then
  docker load -i gx-images.tar
else
  echo "未找到 gx-images.tar，将直接使用已有镜像；若镜像缺失 compose 会尝试构建/拉取"
fi

echo "== [3/5] 创建必要目录 =="
mkdir -p backend/images

echo "== [4/5] 启动服务（生产编排，MySQL 数据持久化到命名卷 mysql-data） =="
docker compose -f docker-compose-prod.yml up -d

echo "== [5/5] 等待就绪并验证 =="
for i in $(seq 1 30); do
  if docker logs gx-app --tail 50 2>/dev/null | grep -q "Started SimpleApplication"; then
    echo "后端启动成功"
    break
  fi
  sleep 2
done

docker compose -f docker-compose-prod.yml ps

IP=$(curl -s --max-time 5 ifconfig.me || echo "服务器IP")
echo ""
echo "============================================"
echo "  部署完成！"
echo "  前端页面 : http://${IP}:8088"
echo "  后端 API : http://${IP}:8080/api"
echo "  查看日志 : docker compose -f docker-compose-prod.yml logs -f app"
echo "============================================"
