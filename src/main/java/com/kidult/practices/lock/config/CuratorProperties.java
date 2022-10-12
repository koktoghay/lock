package com.kidult.practices.lock.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author Created by tommy on 2022/10/12.
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "curator")
public class CuratorProperties {

    // zk地址
    private String connectString;

    // 命名空间
    private String namespace;

    // 会话超时时间
    private int sessionTimeoutMs;

    // 连接超时时间
    private int connectionTimeoutMs;

    // 重试次数
    private int maxRetries;

    // 重试时间间隔
    private int baseSleepTimeMs;

}
