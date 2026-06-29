-- 演示数据充盈补丁（在已有 hospital_his 库上执行）
-- 用法: mysql -u root -p hospital_his < hospital_his_seed_demo.sql
-- 说明: 仅 INSERT 增量，不删表；可重复执行前请先确认无重复键冲突

USE hospital_his;
SET NAMES utf8mb4;

-- 子科室（树形展示）
INSERT INTO sys_department (name, parent_id, sort, status, description) VALUES
('心血管内科', 1, 1, 1, '内科-心血管'),
('呼吸内科', 1, 2, 1, '内科-呼吸'),
('普外科', 2, 1, 1, '外科-普外'),
('创伤骨科', 4, 1, 1, '骨科-创伤');

-- 补充系统用户（用户管理页）
INSERT INTO sys_user (username, password, name, role, role_label, department) VALUES
('pharmacist', '123456', '王药师', 'nurse', '药师', '药房'),
('finance01', '123456', '李会计', 'admin', '财务', '财务部');

-- 补充患者（患者列表/查询/就诊）
INSERT INTO patient (patient_no, name, gender, age, phone, id_card, card_no, address, allergy_history, chronic_disease, status, user_id) VALUES
('P2026001287', '李四', 1, 28, '13900139001', '110101199502021234', 'P2026001287', '上海市浦东新区', '青霉素过敏', '无', 0, NULL),
('P2026001288', '王五', 2, 45, '13900139002', '110101198003031234', 'P2026001288', '广州市天河区', '无', '高血压', 1, NULL),
('P2026001289', '赵六', 1, 62, '13900139003', '110101196505051234', 'P2026001289', '深圳市南山区', '无', '糖尿病', 2, NULL),
('P2026001290', '孙七', 2, 8, '13900139004', '110101201801011234', 'P2026001290', '杭州市西湖区', '无', '无', 0, NULL),
('P2026001291', '周八', 1, 33, '13900139005', '110101199301011234', 'P2026001291', '成都市武侯区', '海鲜过敏', '无', 0, NULL),
('P2026001292', '吴九', 2, 55, '13900139006', '110101197002021234', 'P2026001292', '武汉市江汉区', '无', '冠心病', 1, NULL),
('P2026001293', '郑十', 1, 19, '13900139007', '110101200701011234', 'P2026001293', '南京市鼓楼区', '无', '无', 0, NULL);

-- 补充医生（覆盖皮肤科/口腔科等）
INSERT INTO doctor (doctor_no, name, gender, department, title, specialty, phone, status) VALUES
('D000006', '陈静', 2, '皮肤科', '副主任医师', '皮肤病', '13900006666', 1),
('D000007', '杨帆', 1, '口腔科', '主治医师', '口腔修复', '13900007777', 1),
('D000008', '黄磊', 1, '外科', '主任医师', '肝胆外科', '13900008888', 1),
('D000009', '林娜', 2, '儿科', '主治医师', '小儿呼吸', '13900009999', 1),
('D000010', '马超', 1, '骨科', '副主任医师', '脊柱外科', '13900010001', 1);

-- 补充公告
INSERT INTO notice (title, content, notice_type, publisher, status, publish_time) VALUES
('新院区开放试运行', '东软云医院西院区将于8月1日起试运行，首批开放内科、外科门诊。', '公告', '院办', 1, DATE_SUB(NOW(), INTERVAL 3 DAY)),
('体检套餐优惠活动', '6月28日-7月31日，基础体检套餐8折优惠，详情请咨询健康管理中心。', '通知', '健康管理中心', 1, DATE_SUB(NOW(), INTERVAL 1 DAY)),
('停诊通知', '因设备维护，6月30日下午放射科暂停接诊，请已预约患者改约。', '通知', '门诊部', 1, NOW());

-- 张三（patient_id=1）业务数据充盈 — 用户端/小程序
INSERT INTO register_order (register_no, patient_id, patient_name, department, doctor_id, doctor_name, register_type, fee, status, register_time) VALUES
('GH20260620001', 1, '张三', '外科', 3, '李强', '专家号', 50.00, 0, DATE_SUB(NOW(), INTERVAL 2 DAY)),
('GH20260622001', 1, '张三', '儿科', 2, '王芳', '普通号', 10.00, 1, DATE_SUB(NOW(), INTERVAL 1 DAY)),
('GH20260628001', 1, '张三', '骨科', 4, '赵敏', '普通号', 10.00, 3, NOW());

INSERT INTO appointment (appointment_no, patient_id, patient_name, department, doctor_id, doctor_name, appointment_date, time_slot, status) VALUES
('YY20260625001', 1, '张三', '眼科', 5, '刘洋', DATE_ADD(CURDATE(), INTERVAL 2 DAY), '14:00-15:00', 0),
('YY20260626001', 1, '张三', '皮肤科', 6, '陈静', DATE_ADD(CURDATE(), INTERVAL 3 DAY), '09:00-10:00', 1),
('YY20260610001', 1, '张三', '内科', 1, '张明', DATE_SUB(CURDATE(), INTERVAL 5 DAY), '10:00-11:00', 2);

INSERT INTO payment (payment_no, patient_id, patient_name, item_name, item_type, department, doctor_name, amount, pay_method, status, pay_time, advice, guide_tip, fee_breakdown) VALUES
('JF20260618001', 1, '张三', '住院押金', 'deposit', '内科', '张明', 2000.00, '微信', 1, DATE_SUB(NOW(), INTERVAL 4 DAY), '住院部办理入院', '请携带身份证', '[{"name":"住院押金","amount":2000}]'),
('JF20260624001', 1, '张三', 'B超检查', 'check', '外科', '李强', 120.00, '', 0, NULL, '检查前禁食8小时', '缴费后前往超声科', '[{"name":"腹部B超","amount":120}]'),
('JF20260627001', 1, '张三', '专家挂号费', 'register', '骨科', '赵敏', 50.00, '支付宝', 2, DATE_SUB(NOW(), INTERVAL 1 DAY), '已退款', '退款3个工作日到账', '[{"name":"专家挂号费","amount":50}]');

INSERT INTO medical_record (patient_id, patient_name, doctor_id, doctor_name, department, diagnosis, treatment, visit_time, status, revision_status, revisions) VALUES
(1, '张三', 1, '张明', '内科', '急性支气管炎', '止咳化痰，注意休息', DATE_SUB(NOW(), INTERVAL 10 DAY), 2, 0, '[]'),
(1, '张三', 3, '李强', '外科', '阑尾炎术后复查', '伤口恢复良好', DATE_SUB(NOW(), INTERVAL 30 DAY), 2, 1, '[{"time":"2026-05-20","content":"初诊记录"}]'),
(2, '李四', 1, '张明', '内科', '高血压初诊', '降压药物+低盐饮食', DATE_SUB(NOW(), INTERVAL 3 DAY), 1, 0, '[]'),
(3, '王五', 4, '赵敏', '骨科', '腰椎间盘突出', '理疗+药物', DATE_SUB(NOW(), INTERVAL 7 DAY), 2, 0, '[]'),
(4, '赵六', 2, '王芳', '儿科', '小儿感冒', '对症处理', DATE_SUB(NOW(), INTERVAL 2 DAY), 0, 0, '[]');

-- 全院业务（管理端列表页）
INSERT INTO register_order (register_no, patient_id, patient_name, department, doctor_id, doctor_name, register_type, fee, status, register_time) VALUES
('GH20260628002', 2, '李四', '内科', 1, '张明', '普通号', 10.00, 0, NOW()),
('GH20260628003', 3, '王五', '骨科', 4, '赵敏', '专家号', 50.00, 1, DATE_SUB(NOW(), INTERVAL 1 HOUR)),
('GH20260628004', 5, '孙七', '儿科', 2, '王芳', '普通号', 10.00, 2, DATE_SUB(NOW(), INTERVAL 3 HOUR)),
('GH20260628005', 6, '周八', '皮肤科', 6, '陈静', '普通号', 10.00, 3, DATE_SUB(NOW(), INTERVAL 5 HOUR));

INSERT INTO appointment (appointment_no, patient_id, patient_name, department, doctor_id, doctor_name, appointment_date, time_slot, status) VALUES
('YY20260628002', 2, '李四', '外科', 3, '李强', DATE_ADD(CURDATE(), INTERVAL 1 DAY), '08:00-09:00', 0),
('YY20260628003', 3, '王五', '骨科', 10, '马超', DATE_ADD(CURDATE(), INTERVAL 4 DAY), '15:00-16:00', 1),
('YY20260628004', 7, '郑十', '口腔科', 7, '杨帆', DATE_ADD(CURDATE(), INTERVAL 5 DAY), '11:00-12:00', 0);

INSERT INTO payment (payment_no, patient_id, patient_name, item_name, item_type, department, doctor_name, amount, pay_method, status, pay_time, advice, guide_tip, fee_breakdown) VALUES
('JF20260628001', 2, '李四', '血常规', 'check', '内科', '张明', 35.00, '微信', 1, NOW(), '空腹采血', '检验科2号窗口', '[{"name":"血常规","amount":35}]'),
('JF20260628002', 3, '王五', 'MRI检查', 'check', '骨科', '赵敏', 680.00, '', 0, NULL, '去除金属物品', '影像科预约', '[{"name":"MRI","amount":680}]'),
('JF20260628003', 5, '孙七', '疫苗接种', 'medicine', '儿科', '王芳', 168.00, '支付宝', 1, DATE_SUB(NOW(), INTERVAL 2 HOUR), '留观30分钟', '儿科接种室', '[{"name":"流感疫苗","amount":168}]');

INSERT INTO drug (drug_code, drug_name, specification, manufacturer, unit, price, drug_type, risk_level, archived, status) VALUES
('DR000004', '维生素C片', '100mg*100片', '华润三九', '瓶', 18.00, 'OTC', '普通', 0, 1),
('DR000005', '氯化钠注射液', '250ml', '科伦药业', '瓶', 5.50, '处方药', '普通', 0, 1),
('DR000006', '奥美拉唑', '20mg*14粒', '阿斯利康', '盒', 32.80, '处方药', '普通', 0, 1),
('DR000007', '胰岛素', '300单位', '诺和诺德', '支', 68.00, '处方药', '高危', 0, 1);

INSERT INTO schedule (doctor_id, doctor_name, department, shift_date, shift_type, status) VALUES
(1, '张明', '内科', DATE_ADD(CURDATE(), INTERVAL 2 DAY), '早班', 1),
(1, '张明', '内科', DATE_ADD(CURDATE(), INTERVAL 3 DAY), '晚班', 1),
(2, '王芳', '儿科', DATE_ADD(CURDATE(), INTERVAL 1 DAY), '早班', 1),
(3, '李强', '外科', CURDATE(), '晚班', 1),
(4, '赵敏', '骨科', DATE_ADD(CURDATE(), INTERVAL 2 DAY), '中班', 1),
(6, '陈静', '皮肤科', CURDATE(), '早班', 1),
(7, '杨帆', '口腔科', DATE_ADD(CURDATE(), INTERVAL 1 DAY), '中班', 1),
(8, '黄磊', '外科', DATE_ADD(CURDATE(), INTERVAL 4 DAY), '早班', 1);

INSERT INTO medical_service (service_name, category, department, price, duration, status, fee_item) VALUES
('血常规检查', '检查', '内科', 35.00, 20, 1, '检查费'),
('疫苗接种', '治疗', '儿科', 168.00, 30, 1, '药品费'),
('康复理疗', '治疗', '骨科', 120.00, 45, 1, '检查费'),
('健康体检套餐', '检查', '内科', 580.00, 90, 1, '检查费'),
('急诊留观', '其他', '外科', 200.00, 60, 0, '住院押金');

INSERT INTO bed (bed_no, ward, bed_type, department, status) VALUES
('A103', '内科一病区', '普通床位', '内科', 0),
('A104', '内科一病区', '普通床位', '内科', 2),
('B201', '外科二病区', '普通床位', '外科', 0),
('B202', '外科二病区', 'VIP床位', '外科', 1),
('C301', '儿科病区', '普通床位', '儿科', 0),
('D401', '骨科病区', '监护床位', '骨科', 1),
('E501', 'ICU', 'VIP床位', '外科', 2);

INSERT INTO income_expense (record_no, record_type, category, amount, department, operator, account_id, source_module, source_id, record_time) VALUES
('IE202606005', 'income', '药品收入', 890.00, '药房', 'admin', 1, 'payment', 'JF20260620003', DATE_SUB(NOW(), INTERVAL 2 DAY)),
('IE202606006', 'expense', '设备维护', 3500.00, '后勤', 'admin', 2, 'procurement', 'PO202606003', DATE_SUB(NOW(), INTERVAL 5 DAY)),
('IE202606007', 'income', '专家号收入', 2500.00, '门诊部', 'admin', 1, 'register', 'GH20260628003', DATE_SUB(NOW(), INTERVAL 1 DAY)),
('IE202606008', 'income', '疫苗收入', 1680.00, '儿科', 'admin', 1, 'payment', 'JF20260628003', NOW());

INSERT INTO reimbursement (apply_no, applicant, department, amount, reason, status, apply_time, workflow_json, attachments_json) VALUES
('RB202606002', '王芳', '儿科', 1200.00, '儿科培训进修', 1, DATE_SUB(NOW(), INTERVAL 10 DAY),
 '[{"node":"提交申请","operator":"王芳","status":"done"},{"node":"科室审批","operator":"科室主任","status":"done"},{"node":"财务打款","operator":"财务部","status":"done"}]',
 '["培训发票.pdf","审批单.pdf"]'),
('RB202606003', '李强', '外科', 800.00, '学术会议注册费', 2, DATE_SUB(NOW(), INTERVAL 5 DAY),
 '[{"node":"提交申请","operator":"李强","status":"done"},{"node":"科室审批","operator":"科室主任","status":"done"},{"node":"财务打款","operator":"财务部","status":"rejected"}]',
 '["会议通知.pdf"]');

INSERT INTO settlement (settlement_no, patient_name, total_amount, paid_amount, status, items_json) VALUES
('ST202606002', '王五', 3200.00, 3200.00, 1,
 '[{"name":"住院费","amount":2800},{"name":"护理费","amount":400}]'),
('ST202606003', '赵六', 1500.00, 500.00, 0,
 '[{"name":"诊疗费","amount":1500},{"name":"已付","amount":500}]');

INSERT INTO procurement (order_no, drug_name, quantity, unit_price, supplier, phase, urgent, logistics_no, receipt_note, status, order_time) VALUES
('PO202606003', '维生素C片', 1000, 12.00, '华润三九', 2, 0, 'SF9876543210', '在途运输中', 0, DATE_SUB(NOW(), INTERVAL 3 DAY)),
('PO202606004', '奥美拉唑', 800, 28.00, '国药控股', 3, 0, 'SF5556667778', '全部入库', 1, DATE_SUB(NOW(), INTERVAL 7 DAY)),
('PO202606005', '胰岛素', 200, 55.00, '上药集团', 0, 1, '', '紧急补货申请', 0, NOW());

INSERT INTO inventory (drug_id, drug_code, drug_name, quantity, min_stock, batch_no, expiry_date, warehouse, status) VALUES
(3, 'DR000003', '头孢克肟', 120, 80, 'BN202603', DATE_ADD(CURDATE(), INTERVAL 10 MONTH), '中心药库', 1),
(4, 'DR000004', '维生素C片', 600, 100, 'BN202604', DATE_ADD(CURDATE(), INTERVAL 24 MONTH), '中心药库', 1),
(5, 'DR000005', '氯化钠注射液', 2000, 500, 'BN202605', DATE_ADD(CURDATE(), INTERVAL 18 MONTH), '中心药库', 1),
(6, 'DR000006', '奥美拉唑', 350, 50, 'BN202606', DATE_ADD(CURDATE(), INTERVAL 15 MONTH), '门诊药房', 1),
(7, 'DR000007', '胰岛素', 80, 30, 'BN202607', DATE_ADD(CURDATE(), INTERVAL 6 MONTH), '冷链库', 1);

INSERT INTO dispensing (dispensing_no, prescription_no, barcode, patient_name, drug_name, quantity, pharmacist, priority, status, create_time) VALUES
('DP202606004', 'RX202606004', 'BC202606004', '张三', '奥美拉唑', 1, '王药师', '门诊', 1, DATE_SUB(NOW(), INTERVAL 1 DAY)),
('DP202606005', 'RX202606005', 'BC202606005', '李四', '维生素C片', 2, '王药师', '门诊', 0, NOW()),
('DP202606006', 'RX202606006', 'BC202606006', '王五', '布洛芬', 1, '李药师', '急诊', 0, NOW()),
('DP202606007', 'RX202606007', 'BC202606007', '赵六', '胰岛素', 3, '李药师', '住院', 1, DATE_SUB(NOW(), INTERVAL 2 HOUR));

INSERT INTO sys_dict (dict_type, dict_label, dict_value, sort, status, remark) VALUES
('appointment_status', '待确认', '0', 1, 1, '预约状态'),
('appointment_status', '已确认', '1', 2, 1, '预约状态'),
('appointment_status', '已完成', '2', 3, 1, '预约状态'),
('appointment_status', '已取消', '3', 4, 1, '预约状态'),
('payment_status', '待缴费', '0', 1, 1, '缴费状态'),
('payment_status', '已缴费', '1', 2, 1, '缴费状态'),
('payment_status', '已退款', '2', 3, 1, '缴费状态'),
('register_status', '待就诊', '0', 1, 1, '挂号状态'),
('register_status', '候诊中', '1', 2, 1, '挂号状态'),
('register_status', '已完成', '2', 3, 1, '挂号状态'),
('register_status', '已取消', '3', 4, 1, '挂号状态');

INSERT INTO sys_operation_log (module, action, operator, ip, status) VALUES
('挂号管理', '新增挂号', 'nurse', '127.0.0.1', 1),
('预约管理', '确认预约', 'admin', '127.0.0.1', 1),
('缴费管理', '发起退款', 'admin', '127.0.0.1', 1),
('药房管理', '完成配药', 'pharmacist', '127.0.0.1', 1),
('财务管理', '审批报销', 'admin', '127.0.0.1', 1),
('患者管理', '开始就诊', 'doctor', '127.0.0.1', 1),
('系统管理', '修改配置', 'admin', '127.0.0.1', 1),
('人事管理', '导入医生', 'admin', '127.0.0.1', 1),
('库存管理', '库存调整', 'pharmacist', '127.0.0.1', 1),
('统计分析', '导出报表', 'admin', '127.0.0.1', 1);

INSERT INTO sys_login_log (username, ip, status, message) VALUES
('doctor', '127.0.0.1', 1, '登录成功'),
('nurse', '127.0.0.1', 1, '登录成功'),
('patient', '127.0.0.1', 1, '登录成功'),
('admin', '192.168.1.100', 1, '登录成功'),
('admin', '192.168.1.100', 0, '密码错误'),
('doctor', '10.0.0.5', 1, '登录成功'),
('admin', '127.0.0.1', 1, '登录成功');
