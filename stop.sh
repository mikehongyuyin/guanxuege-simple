#!/usr/bin/env bash
set -e
cd "$(dirname "$0")"
echo "正在停止并移除容器..."
docker compose down
echo "已停止。"
