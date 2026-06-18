package io.hashmatrix.platform.common.platform;

import static org.assertj.core.api.Assertions.assertThat;

import io.hashmatrix.starter.tenant.TenantContext;
import io.hashmatrix.starter.tenant.TenantContextHolder;
import io.hashmatrix.test.fixtures.MockTenants;
import org.junit.jupiter.api.Test;

/**
 * {@link PlatformInfoService} 单测：验证基座信息的<strong>租户感知</strong>与匿名回退。
 *
 * <p>租户标识统一取自脱敏 fixtures（{@link MockTenants}），不出现任何真实租户。
 */
class PlatformInfoServiceTest {

    private final PlatformInfoService service = new PlatformInfoService("platform-common");

    @Test
    void reflectsCurrentTenantFromContext() {
        PlatformInfo info =
                TenantContextHolder.callWith(TenantContext.of(MockTenants.ACME), service::currentInfo);

        assertThat(info.service()).isEqualTo("platform-common");
        assertThat(info.tenant()).isEqualTo(MockTenants.ACME);
    }

    @Test
    void fallsBackToAnonymousWhenNoTenantBound() {
        // 无租户上下文（信息端点非隔离资源）→ 回退占位，不抛错。
        PlatformInfo info = service.currentInfo();

        assertThat(info.tenant()).isEqualTo(PlatformInfo.ANONYMOUS_TENANT);
    }
}
