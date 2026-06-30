-- 医嘱结构化 JSON：医生开单（无价格）→ 护士确认计价
ALTER TABLE medical_record
  ADD COLUMN order_items LONGTEXT NULL AFTER treatment;
