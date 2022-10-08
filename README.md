### 分布式锁实现
* 基于数据库
```锁ddl
-- 分布式锁表
CREATE TABLE t_distributed_lock (
    id            VARCHAR(32)                        NOT NULL COMMENT '主键',
    business_key  VARCHAR(100)                       not null comment '锁的资源(业务主键)',
    business_desc varchar(30)                        not null comment '业务描述',
    create_time   DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    PRIMARY KEY (id)
)
    ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_bin COMMENT ='分布式锁';

alter table t_distributed_lock add unique index idx_business_key(business_key);
```
1. 悲观锁
```商品表ddl
-- 商品表
CREATE TABLE t_goods (
    id          VARCHAR(32)                        NOT NULL COMMENT '主键',
    title       VARCHAR(100)                       not null comment '商品标题',
    stock       int                                not null comment '库存',
    price       decimal(10, 4)                     NOT NULL COMMENT '价格',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '发布时间',
    PRIMARY KEY (id)
)
    ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_bin COMMENT ='商品表';

insert into t_goods(id, title, stock, price, create_time) values (1, '电饭锅', 100, 100, now());
```
在一个事务中，通过for update完成加锁操作，然后在库存大于0的情况下进行更新的操作
 
2. 乐观锁
```
alter table t_goods add version int default 0 not null comment '商品变更版本号' after price;
```
通过版本号约束并发更新

* 基于redis

* 基于redisson

* 基于zookeeper

* 基于curator framework

