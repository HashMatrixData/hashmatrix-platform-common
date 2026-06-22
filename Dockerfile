# syntax=docker/dockerfile:1
# 多阶段构建：build 阶段经 Maven 坐标解析公共依赖并打可执行 fat-jar，run 阶段仅装 JRE + jar。
#
# 公共依赖在 GitHub Packages，build 阶段需凭据：用 BuildKit secret 挂载 Maven settings.xml，
# 避免 token 落入镜像层（红线）。本地直接 `mvn` 构建时无需该 secret（用开发者自身 ~/.m2/settings.xml）。
# 示例：
#   DOCKER_BUILDKIT=1 docker build --secret id=maven_settings,src=$HOME/.m2/settings.xml -t platform-common:local .
# CI 注入见 .github/workflows/publish-image.yml；docker-compose 本地构建见 docker-compose.local.yml 的 secrets。

# ---- build ----
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY pom.xml ./
COPY src ./src
RUN --mount=type=secret,id=maven_settings,target=/root/.m2/settings.xml \
    --mount=type=cache,target=/root/.m2/repository \
    mvn -B -ntp -DskipTests clean package

# ---- run ----
FROM eclipse-temurin:17-jre-alpine AS run
WORKDIR /app

# 非 root 运行。USER 必须为「数字 uid」：K8s restricted 安全上下文（runAsNonRoot:true 且不带 runAsUser）
# 要在不启动容器的前提下验证 uid≠0，非数字用户名解析不出 uid → kubelet 拒绝创建容器
# （CreateContainerConfigError，主仓 #18）。故 -u/-g 显式钉死 uid 与 gid，并用数字 USER：
#   · 选 10001 远离系统区(0-999) 与基础镜像约定 uid 1000，规避未来占用冲突；
#   · USER 仅写数字 uid（不写 :gid）——运行期 gid 由 app 用户主组(10001)决定，inspect .Config.User 恰为纯数字。
RUN addgroup -S -g 10001 app && adduser -S -u 10001 -G app app

# 复制可执行 fat-jar（spring-boot-maven-plugin repackage 产物；瘦 jar 为 *.jar.original，不匹配此 glob）。
COPY --from=build /workspace/target/platform-common-*.jar app.jar
USER 10001

# 端口基线（主仓 M1 §3）：应用 HTTP 8089 / 管理(actuator) 9089（management.server.port 独立）；
# 运行期可经 SERVER_PORT / MANAGEMENT_SERVER_PORT 覆盖。
EXPOSE 8089 9089

# 健康探针：命中管理端口的 actuator liveness（无外部依赖）。alpine 自带 busybox wget，免装额外包。
HEALTHCHECK --interval=15s --timeout=3s --start-period=40s --retries=5 \
  CMD wget -qO- "http://127.0.0.1:${MANAGEMENT_SERVER_PORT:-9089}/actuator/health/liveness" || exit 1

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
