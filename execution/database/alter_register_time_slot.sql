-- 为当日挂号增加时段字段，使号源统计与预约共用同一套扣减逻辑
ALTER TABLE register_order
  ADD COLUMN time_slot VARCHAR(32) NULL COMMENT '挂号时段' AFTER register_time;
