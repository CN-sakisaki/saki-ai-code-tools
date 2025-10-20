create database saki_ai_code_tools;
use saki_ai_code_tools;


drop database saki_ai_code_tools;
drop table user;


create table user
(
    id              bigint auto_increment comment '主键'
        primary key,
    user_account    varchar(256)                           not null comment '用户账号',
    user_password   varchar(256)                           not null comment '账号密码',
    user_name       varchar(256)                           not null comment '用户名',
    user_email      varchar(256)                           null comment '用户邮箱',
    user_phone      varchar(256)                           null comment '用户手机号',
    user_avatar     varchar(512)                           not null comment '用户头像',
    user_profile    varchar(512)                           not null comment '用户简介',
    user_role       varchar(256) default 'user'            not null comment '用户角色（user/admin）',
    user_status     tinyint      default 0                 not null comment '用户状态（0-禁用，1-状态）',
    is_vip          tinyint      default 0                 not null comment '是否为会员（0-普通会员，1-vip 会员）',
    vip_start_time  datetime                               null comment 'vip 会员开始时间',
    vip_end_time    datetime                               null comment 'vip 会员结束时间',
    invite_code     varchar(32)                            null comment '邀请码',
    last_login_time datetime                               null comment '最近一次登录的时间',
    last_login_ip   varchar(64)                            null comment '最近一次登录的 IP 地址',
    edit_time       datetime                               null comment '最近一次编辑时间',
    create_time     datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time     datetime     default CURRENT_TIMESTAMP not null comment '更新时间',
    user_salt       varchar(16)                            null comment '盐值',
    is_delete       tinyint      default 0                 not null comment '逻辑删除（0-未删除，1-已删除）',
    constraint uk_user_account_isdelete
        unique (user_account, is_delete),
    constraint uk_user_email_isdelete
        unique (user_email, is_delete),
    constraint uk_user_phone_isdelete
        unique (user_phone, is_delete)
)
    comment '用户表' collate = utf8mb4_unicode_ci;

create index idx_is_vip
    on user (is_vip);

create index idx_user_role
    on user (user_role);

create index idx_user_status
    on user (user_status);


