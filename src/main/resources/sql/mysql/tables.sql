CREATE TABLE oa_qq_robot (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  app_id bigint(20) DEFAULT NULL COMMENT '应用编号',
  email varchar(50) DEFAULT NULL COMMENT '账号',
  call_persons bigint(15) DEFAULT '0' COMMENT '访问人数',
  ad_send bigint(15) DEFAULT '0' COMMENT '广告投放',
  ua_wait decimal(3,2) DEFAULT '0.00' COMMENT '活跃留存',
  uw_time char(10) DEFAULT '0' COMMENT '停留时长(s)',
  ad_income decimal(10,2) DEFAULT '0.00' COMMENT '广告收入',
  qq_channel decimal(10,2) DEFAULT '0.00' COMMENT 'Q自有渠道',
  buy_channel decimal(10,2) DEFAULT '0.00' COMMENT '买量渠道',
  handle_date varchar(10) DEFAULT NULL COMMENT '数据时间(yyyyMMdd)',
  valid tinyint(1) DEFAULT '1' COMMENT '是否有效(1-y,0-n)',
  create_time timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uniques (app_id,handle_date,valid) USING BTREE COMMENT '联合唯一索引,便于重复保存做更新操作.'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Q小程序开放平台';