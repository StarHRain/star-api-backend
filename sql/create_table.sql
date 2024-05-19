use star_api;

create database if not exists star;
use star;

create table if not exists user
(
    id           bigint(8) auto_increment comment 'id'
        primary key,
    userAccount  varchar(256)                           not null comment '账号',
    userPassword varchar(512)                           not null comment '密码',
    unionId      varchar(256)                           null comment '微信开放平台id',
    mpOpenId     varchar(256)                           null comment '公众号openId',
    userName     varchar(256)                           null comment '用户昵称',
    userAvatar   varchar(1024)                          null comment '用户头像',
    userProfile  varchar(512)                           null comment '用户简介',
    userRole     varchar(256) default 'user'            not null comment '用户角色：user/admin/ban',
    balance      int          default 0                 not null comment '用户余额',
    accessKey    varchar(512)                           not null comment 'accessKey',
    secretKey    varchar(512)                           not null comment 'secretKey',
    createTime   datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint      default 0                 not null comment '是否删除'
) comment '用户' collate = utf8mb4_unicode_ci;


CREATE TABLE IF NOT EXISTS interface_info
(
    id                   BIGINT AUTO_INCREMENT COMMENT '主键' PRIMARY KEY,
    name                 VARCHAR(256)                       NOT NULL COMMENT '名称',
    description          VARCHAR(256)                       NULL COMMENT '描述',
    url                  VARCHAR(512)                       NOT NULL COMMENT '接口地址',
    host                 VARCHAR(512)                       NULL COMMENT '主机名',
    price                int      default 10                not null COMMENT '购买1000次调用次数的价格',
    requestParams        TEXT                               NULL COMMENT '请求参数',
    requestParamsRemark  TEXT                               NULL COMMENT '请求参数说明',
    responseParamsRemark TEXT                               NULL COMMENT '响应参数说明',
    requestHeader        TEXT                               NULL COMMENT '请求头',
    responseHeader       TEXT                               NULL COMMENT '响应头',
    status               INT      DEFAULT 0                 NOT NULL COMMENT '接口状态（0-关闭，1-开启）',
    method               VARCHAR(256)                       NOT NULL COMMENT '请求类型',
    userId               BIGINT                             NOT NULL COMMENT '创建人',
    createTime           DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    updateTime           DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    isDelete             TINYINT  DEFAULT 0                 NOT NULL COMMENT '是否删除(0-未删, 1-已删)'
) COMMENT '接口信息' DEFAULT CHARSET = utf8mb4
                     COLLATE = utf8mb4_unicode_ci;


create table if not exists user_interface_info
(
    id              bigint auto_increment comment '主键'
        primary key,
    userId          bigint                             not null comment '调用用户 id',
    interfaceInfoId bigint                             not null comment '接口 id',
    totalNum        int      default 0                 not null comment '总调用次数',
    leftNum         int      default 0                 not null comment '剩余调用次数',
    status          int      default 0                 not null comment '0-正常，1-禁用',
    createTime      datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime      datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete        tinyint  default 0                 not null comment '是否删除(0-未删, 1-已删)'
) comment '用户调用接口关系';

-- 图表表
create table if not exists chart
(
    id          bigint auto_increment comment 'id' primary key,
    goal        text                               null comment '分析目标',
    `name`      varchar(128)                       null comment '图表名称',
    chartData   text                               null comment '图表数据',
    chartType   varchar(128)                       null comment '图表类型',
    genChart    text                               null comment '生成的图表数据',
    genResult   text                               null comment '生成的分析结论',
    status      varchar(128)                       not null default 'wait' comment 'wait,running,succeed,failed',
    execMessage text                               null comment '执行信息',
    userId      bigint                             null comment '创建用户 id',
    createTime  datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime  datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete    tinyint  default 0                 not null comment '是否删除'
) comment '图表信息表' collate = utf8mb4_unicode_ci;


-- 图片分析表
create table if not exists image
(
    id         bigint auto_increment comment 'id' primary key,
    goal       text                               null comment '分析目标',
    imageType  varchar(512)                       null comment '图片类型',
    genResult  text                               null comment '生成的分析结论',
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete   tinyint  default 0                 not null comment '是否删除'
) comment '图片分析表' collate = utf8mb4_unicode_ci;




