-- 存量库升级：sys_operation_log 增加前端上报字段（仅需执行一次）
ALTER TABLE sys_operation_log
    ADD COLUMN source VARCHAR(16) DEFAULT 'backend' AFTER status,
    ADD COLUMN client VARCHAR(16) AFTER source,
    ADD COLUMN path VARCHAR(255) AFTER client,
    ADD COLUMN detail VARCHAR(512) AFTER path;

UPDATE sys_operation_log SET source = 'backend' WHERE source IS NULL OR source = '';
