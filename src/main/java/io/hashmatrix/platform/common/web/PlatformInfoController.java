package io.hashmatrix.platform.common.web;

import io.hashmatrix.platform.common.platform.PlatformInfo;
import io.hashmatrix.platform.common.platform.PlatformInfoService;
import io.hashmatrix.starter.web.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 平台基座信息端点（Web 层）。
 *
 * <p>统一以 {@link ApiResponse} 返回（由 {@code hashmatrix-starter-web} 约定）；异常经全局处理器统一转换。
 * 仅暴露脱敏自描述信息，不承载任何业务/客户数据。
 */
@RestController
@RequestMapping("/api/platform")
public class PlatformInfoController {

    private final PlatformInfoService platformInfoService;

    public PlatformInfoController(PlatformInfoService platformInfoService) {
        this.platformInfoService = platformInfoService;
    }

    /** 返回当前请求视角的平台基座信息（含当前租户标注）。 */
    @GetMapping("/info")
    public ApiResponse<PlatformInfo> info() {
        return ApiResponse.ok(platformInfoService.currentInfo());
    }
}
