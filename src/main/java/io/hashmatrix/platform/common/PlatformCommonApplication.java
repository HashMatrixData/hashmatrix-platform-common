package io.hashmatrix.platform.common;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 平台横切公共能力服务入口。
 *
 * <p>提供调度 / 工作流 / 统一元数据的工程基座；公共能力一律<strong>租户感知</strong>，不做跨租户默认共享。
 * 租户上下文由 {@code hashmatrix-starter-tenant} 经 {@code X-Tenant-*} 头透传（架构 05 §5），
 * 统一返回 / 异常 / 审计 / 可观测由对应 {@code hashmatrix-starter-*} 自动装配。
 *
 * <p>职责边界：<strong>不</strong>做租户生命周期 / 开通 / 配额（归 {@code control-plane}），
 * 亦不实现单一分系统业务逻辑。
 */
@SpringBootApplication
public class PlatformCommonApplication {

    public static void main(String[] args) {
        SpringApplication.run(PlatformCommonApplication.class, args);
    }
}
