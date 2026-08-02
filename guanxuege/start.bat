@echo off
setlocal
cd /d "%~dp0"

echo ============================================
echo   观学阁 GuanXueGe 一键启动
echo ============================================
echo   [1] 仅启动中间件 (MySQL / Redis / RocketMQ / Nacos)
echo   [2] 全栈启动 (中间件 + 后端5服务 + 前端, 首次会构建镜像)
echo ============================================
set /p CHOICE=请选择 [1/2]，默认 2: 
if "%CHOICE%"=="1" goto :middleware
goto :fullstack

:middleware
echo.
echo 正在启动中间件...
docker compose up -d
echo.
echo 中间件已启动。访问:
echo   Nacos 控制台 : http://localhost:8848/nacos
echo   MySQL        : localhost:3306  (root/root)
goto :done

:fullstack
echo.
echo 正在构建并启动全栈 (首次较慢，需联网拉取 Maven/npm 依赖)...
docker compose --profile app up -d --build
echo.
echo 全栈已启动。访问:
echo   前端页面     : http://localhost:8088
echo   网关 API     : http://localhost:8080
echo   Nacos 控制台 : http://localhost:8848/nacos
echo.
echo 查看日志: docker compose --profile app logs -f
goto :done

:done
echo.
pause
