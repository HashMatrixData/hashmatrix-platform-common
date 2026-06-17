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
- **不做（边界）**：不做**租户生命周期/开通**（那是 `control-plane`）；不做某一分系统的业务逻辑。

## 骨架技术选型（首选 · 待逐仓细化）

| 维度 | 选型 |
|--|--|
| 运行时 | Spring Boot（Java） |
| 调度 | **DolphinScheduler** |
| 工作流引擎 | **Flowable** |
| 公共依赖 | 主仓 `libs-java`（parent + BOM + `starter-tenant`） |
| 业务库 | PostgreSQL |

> 公共上下文能力（如 `starter-tenant` 租户透传）的契约在此与主仓 `libs-java` 对齐。

## 产品形态与多租户（北极星）

**双模交付**：公网 SaaS（我们运营 · 统一**我们品牌** · 租户=企业）／私有化部署（客户环境 · **客户品牌**部署级 · 租户=客户部门）。品牌**部署级**、不按租户运行期换肤。多租户走 **C 分层桥接**：控制平面共享 + 数据平面按租户隔离（Keycloak Organizations 单 realm · schema/db-per-tenant · namespace-per-tenant），由 `control-plane` 编排开通。

**本仓视角**：公共能力**租户感知**，透传租户上下文、不跨租户默认共享。

> 详见主仓 `docs/00-主仓初始化-spec.md`、`docs/architecture/05-多租户与控制平面.md`。

## 说明

本仓库作为 `hashmatrix` 主仓的 git submodule，挂载于 `services/platform-common`。架构背景见主仓 `docs/architecture/`。

## License

Apache-2.0
