# 观学阁单体应用多阶段构建
# 例：docker build -f Dockerfile -t gx-app .

# ---------- 构建阶段 ----------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /workspace
# 先拷贝 pom（利用层缓存，依赖不变时无需重新下载）
COPY pom.xml .
RUN mvn -q -B dependency:go-offline || true
# 再拷贝源码并打包
COPY . .
RUN mvn -q -B package -DskipTests

# ---------- 运行阶段 ----------
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /workspace/target/guanxuege-simple.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
