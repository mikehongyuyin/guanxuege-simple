@echo off
setlocal
cd /d "%~dp0"

echo ============================================
echo   观学阁 GuanXueGe 单体版 一键启动
echo ============================================
echo   [1] 仅启动中间件 (MySQL)
echo   [2] 全栈启动 (单体后端 + 前端, 首次会构建镜像)
echo ============================================
set /p CHOICE=请选择 [1/2]，默认 2: 
if "%CHOICE%"=="1" goto :middleware
goto :fullstack

:middleware
echo.
echo 正在启动中间件...
docker compose up -d mysql
echo.
echo 中间件已启动。MySQL: localhost:3306 (root/123456)
goto :done

:fullstack
echo.
echo 正在构建并启动全栈 (首次较慢，需联网拉取 Maven/npm 依赖)...
docker compose up -d --build
echo.
echo 全栈已启动。访问:
echo   前端页面     : http://localhost:8088
echo   后端 API     : http://localhost:8080
echo.
echo 查看日志: docker compose logs -f app
goto :done

:done
echo.
pause
