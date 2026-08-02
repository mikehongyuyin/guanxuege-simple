@echo off
setlocal
cd /d "%~dp0"
echo 正在停止并移除容器...
docker compose --profile app down
echo 已停止。
pause
