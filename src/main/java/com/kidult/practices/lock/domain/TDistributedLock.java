package com.kidult.practices.lock.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 分布式锁
 * </p>
 *
 * @author tommy
 * @since 2022-09-29
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class TDistributedLock implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    private String id;

    /**
     * 锁的资源(业务主键)
     */
    private String businessKey;

    /**
     * 业务描述
     */
    private String businessDesc;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

}
