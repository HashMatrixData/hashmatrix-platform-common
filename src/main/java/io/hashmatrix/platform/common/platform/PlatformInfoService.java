package io.hashmatrix.platform.common.platform;

import io.hashmatrix.starter.tenant.TenantContextHolder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 平台基座信息服务（应用层）。
 *
 * <p>演示<strong>租户感知</strong>的最小读取路径：从 {@link TenantContextHolder} 取当前租户做信息标注。
 * 此端点为公共信息端点（非租户隔离资源），故租户上下文缺省时回退占位，而非抛错；
 * 访问真正的租户隔离资源时应改用 {@code TenantContextHolder.requireTenantId()}。
 */
@Service
public class PlatformInfoService {

    private final String serviceName;

    public PlatformInfoService(@Value("${spring.application.name:platform-common}") String serviceName) {
        this.serviceName = serviceName;
    }

    /** 当前请求视角的平台基座信息（租户取自上下文，缺省回退匿名占位）。 */
    public PlatformInfo currentInfo() {
        String tenant = TenantContextHolder.getTenantId().orElse(PlatformInfo.ANONYMOUS_TENANT);
        return new PlatformInfo(serviceName, tenant);
    }
}
