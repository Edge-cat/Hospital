-- 诊疗费账单关联病历，缴费完成后解锁患者端可见
ALTER TABLE payment
  ADD COLUMN record_id BIGINT NULL AFTER register_no;
