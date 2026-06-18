package io.hashmatrix.platform.common.platform;

/**
 * 平台基座自描述信息（脱敏、无业务数据）。
 *
 * @param service 服务名
 * @param tenant  当前请求所属租户标识；无租户上下文时为 {@code "anonymous"}
 */
public record PlatformInfo(String service, String tenant) {

    /** 无租户上下文时的占位标识（信息性，非隔离键）。 */
    public static final String ANONYMOUS_TENANT = "anonymous";
}
