package com.kidult.practices.lock.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Created by tommy on 2022/10/12.
 */
@Slf4j
@Configuration
public class CuratorConfiguration {

    @Autowired
    private CuratorProperties curatorProperties;

    @Bean
    public CuratorFramework curatorFramework() {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(curatorProperties.getBaseSleepTimeMs(), curatorProperties.getMaxRetries());
        CuratorFramework curatorFramework = CuratorFrameworkFactory.builder()
                .connectString(curatorProperties.getConnectString())
                .sessionTimeoutMs(curatorProperties.getConnectionTimeoutMs())
                .connectionTimeoutMs(curatorProperties.getConnectionTimeoutMs())
                .namespace(curatorProperties.getNamespace())
                .retryPolicy(retryPolicy)
                .build();
        curatorFramework.getConnectionStateListenable().addListener(new ConnectionStateListener() {
            @Override
            public void stateChanged(CuratorFramework curatorFramework, ConnectionState connectionState) {
                if (ConnectionState.CONNECTED == connectionState) {
                    log.info("连接成功");
                }
            }
        });
        curatorFramework.start();
        return curatorFramework;
    }
}
