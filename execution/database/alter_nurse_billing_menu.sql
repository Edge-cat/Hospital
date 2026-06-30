-- 护士菜单：移除「医生开始就诊」，新增「医嘱扣费确认」
INSERT INTO sys_menu (id, parent_id, name, path, icon, sort, status)
SELECT 25, 2, '医嘱扣费确认', '/patient/billing-confirm', NULL, 5, 1
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id = 25);

DELETE FROM sys_role_menu WHERE role_code = 'nurse' AND menu_id = 24;
INSERT IGNORE INTO sys_role_menu (role_code, menu_id) VALUES ('nurse', 25);
INSERT IGNORE INTO sys_role_menu (role_code, menu_id) VALUES ('admin', 25);
