# 多阶段构建：用 build-arg SERVICE 指定要打包的微服务模块
# 例：docker build -f Dockerfile --build-arg SERVICE=auth -t gx-auth .

# ---------- 构建阶段 ----------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /workspace
# 先拷贝 pom（利用层缓存，依赖不变时无需重新下载）
COPY pom.xml .
COPY guanxuege-common/pom.xml guanxuege-common/
COPY guanxuege-gateway/pom.xml guanxuege-gateway/
COPY guanxuege-auth/pom.xml guanxuege-auth/
COPY guanxuege-learning-plan/pom.xml guanxuege-learning-plan/
COPY guanxuege-grade/pom.xml guanxuege-grade/
COPY guanxuege-community/pom.xml guanxuege-community/
RUN mvn -q -B -pl guanxuege-${SERVICE} -am dependency:go-offline || true
# 再拷贝源码并打包
COPY . .
ARG SERVICE
RUN mvn -q -B -pl guanxuege-${SERVICE} -am package -DskipTests

# ---------- 运行阶段 ----------
FROM eclipse-temurin:17-jre
WORKDIR /app
ARG SERVICE
COPY --from=build /workspace/guanxuege-${SERVICE}/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
