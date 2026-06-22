# hashmatrix-platform-common

> hashmatrix 数据中台子模块 · 所属：横切 · 平台公共能力
>
> 主仓：[HashMatrixData/hashmatrix](https://github.com/HashMatrixData/hashmatrix)

## 角色与位置（一眼看懂）

- **所属**：横切层 · 各分系统共享的平台公共能力（无状态 Spring Boot 应用）。
- **一句话**：平台的"公共底盘"——调度 / 工作流引擎 / 统一元数据等被多个分系统复用的能力。
- **协作**：governance / security / data-foundation 等 → 复用 **platform-common（调度/工作流/元数据）**。

## 职责与边界

- **做**：作业调度（DolphinScheduler）、工作流引擎（Flowable）、统一元数据、其它跨分系统公共服务。
- **不做（边界）**：不做**租户生命周期/开通/配额**（那是 `control-plane`）；不实现某一分系统的业务逻辑。公共能力一律**租户感知**、不跨租户默认共享。

## 骨架技术选型（首选 · 待逐仓细化）

| 维度 | 选型 |
|--|--|
| 运行时 | **Spring Boot 3.3.5（Java 17）**——经主仓 `hashmatrix-bom` 钉死，升级=改 BOM 一行 |
| 调度 | **DolphinScheduler** |
| 工作流引擎 | **Flowable** |
| 公共依赖 | 主仓 `libs-java`：`hashmatrix-platform-parent` + import `hashmatrix-bom` + `hashmatrix-starter-{tenant,web,audit,observability,test}` |
| 业务库 | PostgreSQL |

> 公共上下文能力（如 `starter-tenant` 租户透传）的契约在此与主仓 `libs-java` 对齐。

## 产品形态与多租户（北极星）

**双模交付**：公网 SaaS（我们运营 · 统一**我们品牌** · 租户=企业）／私有化部署（客户环境 · **客户品牌**部署级 · 租户=客户部门）。品牌**部署级**、不按租户运行期换肤。多租户走 **C 分层桥接**：控制平面共享 + 数据平面按租户隔离（Keycloak Organizations 单 realm · schema/db-per-tenant · namespace-per-tenant），由 `control-plane` 编排开通。

**本仓视角**：公共能力**租户感知**，透传租户上下文、不跨租户默认共享。

> 详见主仓 `docs/00-主仓初始化-spec.md`、`docs/architecture/05-多租户与控制平面.md`。

## 构建与运行

需 **JDK 17 + Maven 3.8+**，且能访问制品仓（GitHub Packages，拉取需带 `read:packages` 的 PAT，配 `~/.m2/settings.xml` 的 `id=github` server；内网用 `-Pxinchuang` 切私服）。

```bash
mvn -DskipTests package          # 单测随 package 跑（surefire）；产出可执行 fat-jar
mvn verify                       # 追加 Testcontainers 集成测试（需 Docker）

# 本地独立起栈（PostgreSQL + 服务；镜像 build 阶段经 ~/.m2/settings.xml 解析公共依赖，见 docker-compose 文末 secrets）
docker compose -f docker-compose.local.yml up --build
curl -fsS http://localhost:9089/actuator/health     # 期望 200，status=UP（健康检查在管理端口 9089）
```

端口基线（主仓 M1 §3）：应用 HTTP **8089**、管理/actuator **9089**（`management.server.port` 独立）；均 `${SERVER_PORT}` / `${MANAGEMENT_SERVER_PORT}` 可覆盖。
端点：应用 `:8089` 上 `GET /api/platform/info`（统一 `ApiResponse`，含当前租户标注）；管理 `:9089` 上 `/actuator/health`（含 `health/liveness`、`health/readiness`）、`/actuator/info`、`/actuator/prometheus`。

> ⚠️ **当前依赖门控**：基线 pin 到 **libs-java v0.2.0**（含 `starter-audit` / `starter-observability`）。
> v0.2.0 发布到 GitHub Packages 前，`mvn package` 无法解析 parent/bom/audit/observability，构建会红——
> 属预期状态，非本仓代码问题。v0.2.0 就绪后即可绿。详见 issue #1 与主仓 #1。

## 说明

本仓库作为 `hashmatrix` 主仓的 git submodule，挂载于 `services/platform-common`。架构背景见主仓 `docs/architecture/`；
跨子系统集成契约见主仓 `contracts/`（本仓为 `icd/tenant-context-headers` 及 governance-metadata 系列 `icd/governance-metadata` · `openapi/governance-metadata-v1` · `asyncapi/governance-metadata` 的消费方，详见 `CLAUDE.md`）。

## License

Apache-2.0
