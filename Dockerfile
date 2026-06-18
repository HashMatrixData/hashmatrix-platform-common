# 运行时镜像：仅承载已构建的 fat-jar（构建在 CI/本地以 `mvn package` 完成）。
# 把构建与镜像分离，避免在 docker build 内引入 GitHub Packages 凭据（拉取公共 starter 需 read:packages PAT）。
#
# 用法：
#   mvn -DskipTests package
#   docker build -t platform-common:local .
FROM eclipse-temurin:17-jre AS runtime

# curl 供容器 HEALTHCHECK 使用（temurin jre 基础镜像默认不含）。
RUN apt-get update \
  && apt-get install -y --no-install-recommends curl \
  && rm -rf /var/lib/apt/lists/*

# 以非 root 运行（最小权限）。
RUN groupadd --system app && useradd --system --gid app --home /app app
WORKDIR /app

# 仅拷贝可执行 fat-jar（spring-boot-maven-plugin repackage 产物）。
ARG JAR_FILE=target/platform-common-*.jar
COPY ${JAR_FILE} app.jar
RUN chown -R app:app /app
USER app

EXPOSE 8080

# 健康探针：命中 actuator liveness（无外部依赖）。
HEALTHCHECK --interval=15s --timeout=3s --start-period=40s --retries=5 \
  CMD curl -fsS http://127.0.0.1:8080/actuator/health/liveness || exit 1

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
