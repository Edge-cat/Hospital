-- 科室编码字段（与后端 SysDepartment.code 对齐）
ALTER TABLE sys_department
  ADD COLUMN code VARCHAR(16) NULL AFTER name;

-- 按 id 回填（避免 PowerShell 管道中文编码问题；id 与 seed 数据一致）
UPDATE sys_department SET code = 'NK' WHERE id = 1;
UPDATE sys_department SET code = 'WK' WHERE id = 2;
UPDATE sys_department SET code = 'EK' WHERE id = 3;
UPDATE sys_department SET code = 'GK' WHERE id = 4;
UPDATE sys_department SET code = 'YK' WHERE id = 5;
UPDATE sys_department SET code = 'PFK' WHERE id = 6;
UPDATE sys_department SET code = 'KQK' WHERE id = 7;
UPDATE sys_department SET code = 'XGNK' WHERE id = 8;
UPDATE sys_department SET code = 'HXNK' WHERE id = 9;
UPDATE sys_department SET code = 'PWK' WHERE id = 10;
UPDATE sys_department SET code = 'CSGK' WHERE id = 11;
