package io.hashmatrix.platform.common;

import static org.assertj.core.api.Assertions.assertThat;

import io.hashmatrix.starter.tenant.TenantContext;
import io.hashmatrix.starter.tenant.TenantContextHolder;
import io.hashmatrix.test.fixtures.MockTenants;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * 集成切片：用 Testcontainers 起真实 PostgreSQL，验证「整应用上下文可加载 + 健康检查通过 + 端点连通」。
 *
 * <p>对应 issue 验收：{@code docker-compose up → /actuator/health 200 → 集成测试绿} 的工程内等价校验
 * （CI/本地以 Testcontainers 替代 docker-compose，免手工起依赖）。需要 Docker 可用；由 failsafe 在 verify 阶段执行。
 *
 * <p>容器口令为<strong>临时脱敏占位</strong>（仅测试 JVM 内可见，容器随测试销毁），不入任何配置/制品。
 */
// 独立管理端口（application.yml: management.server.port=9089）会把 actuator 搬到 TestRestTemplate
// 够不到的管理子上下文（→ /actuator/** 404）。测试期置空 management.server.port，把 actuator 收回主
// （随机）端口上下文，使 /actuator/health 经 TestRestTemplate 可达；真实双端口绑定由 Dockerfile/compose/k8s 覆盖。
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "management.server.port=")
@Testcontainers
class PlatformCommonApplicationIT {

    @Container
    @SuppressWarnings("resource") // 容器生命周期由 @Testcontainers 托管，无需手工 close
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("platform_common")
                    .withUsername("platform")
                    .withPassword("platform-test");

    @DynamicPropertySource
    static void datasourceProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired
    private TestRestTemplate rest;

    @Test
    void healthEndpointIsUp() {
        ResponseEntity<String> response = rest.getForEntity("/actuator/health", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("\"status\":\"UP\"");
    }

    @Test
    void platformInfoEndpointRespondsWithTenantAwareApiResponse() {
        // 无网关注入租户头 → 信息端点回退匿名占位（非隔离资源），统一 ApiResponse 包裹。
        ResponseEntity<String> response = rest.getForEntity("/api/platform/info", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("\"code\":\"0\"");
        assertThat(response.getBody()).contains("\"tenant\":\"anonymous\"");
    }

    @Test
    void tenantContextPropagatesWithinExecution() {
        // 校验租户上下文传播（脱敏 fixtures）；与端点解耦，纯应用层。
        String tenant =
                TenantContextHolder.callWith(
                        TenantContext.of(MockTenants.ACME), TenantContextHolder::requireTenantId);
        assertThat(tenant).isEqualTo(MockTenants.ACME);
    }
}
