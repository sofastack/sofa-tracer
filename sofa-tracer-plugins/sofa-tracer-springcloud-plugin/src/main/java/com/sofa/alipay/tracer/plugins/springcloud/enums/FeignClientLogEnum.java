package com.sofa.alipay.tracer.plugins.springcloud.enums;

/**
 * @author: guolei.sgl (guolei.sgl@antfin.com) 2019/3/13 2:46 PM
 * @since:
 **/
public enum FeignClientLogEnum {
    // Feign Client Digest Log
    FEIGN_CLEINT_DIGEST("feign_digest_log_name", "feign-digest.log",
            "feign_digest_rolling"),
    // Feign Client Stat Log
    FEIGN_CLEINT_STAT("feign_stat_log_name", "feign-stat.log",
            "feign_stat_rolling");

    private String logNameKey;
    private String defaultLogName;
    private String rollingKey;

    FeignClientLogEnum(String logNameKey, String defaultLogName, String rollingKey) {
        this.logNameKey = logNameKey;
        this.defaultLogName = defaultLogName;
        this.rollingKey = rollingKey;
    }

    public String getLogNameKey() {
        //log reserve config key
        return logNameKey;
    }

    public String getDefaultLogName() {
        return defaultLogName;
    }

    public String getRollingKey() {
        return rollingKey;
    }
}
