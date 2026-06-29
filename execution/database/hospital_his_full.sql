-- Hospital HIS MySQL full init
-- Usage: mysql -u root -p < hospital_his_full.sql

CREATE DATABASE IF NOT EXISTS hospital_his DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE hospital_his;
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 东软云医院 HIS 数据库结构（MySQL / H2 兼容）

CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(64) NOT NULL UNIQUE,
    password VARCHAR(128) NOT NULL,
    name VARCHAR(64) NOT NULL,
    role VARCHAR(32) NOT NULL,
    role_label VARCHAR(32),
    department VARCHAR(64),
    phone VARCHAR(20),
    status TINYINT DEFAULT 1,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS sys_department (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(64) NOT NULL,
    parent_id BIGINT DEFAULT 0,
    sort INT DEFAULT 0,
    status TINYINT DEFAULT 1,
    description VARCHAR(255),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS patient (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_no VARCHAR(32),
    name VARCHAR(64) NOT NULL,
    gender TINYINT,
    age INT,
    phone VARCHAR(20),
    id_card VARCHAR(32),
    card_no VARCHAR(32),
    address VARCHAR(255),
    department VARCHAR(64),
    allergy_history VARCHAR(255),
    chronic_disease VARCHAR(255),
    status TINYINT DEFAULT 0,
    user_id BIGINT,
    wx_openid VARCHAR(64),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS doctor (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    doctor_no VARCHAR(32),
    name VARCHAR(64) NOT NULL,
    gender TINYINT,
    department VARCHAR(64),
    title VARCHAR(32),
    specialty VARCHAR(128),
    phone VARCHAR(20),
    status TINYINT DEFAULT 1,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS notice (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(128) NOT NULL,
    content LONGTEXT,
    notice_type VARCHAR(32),
    publisher VARCHAR(64),
    status TINYINT DEFAULT 1,
    publish_time TIMESTAMP,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS register_order (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    register_no VARCHAR(32) NOT NULL,
    patient_id BIGINT,
    patient_name VARCHAR(64),
    department VARCHAR(64),
    doctor_id BIGINT,
    doctor_name VARCHAR(64),
    register_type VARCHAR(32),
    fee DECIMAL(10,2),
    status TINYINT DEFAULT 0,
    register_time TIMESTAMP,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS appointment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    appointment_no VARCHAR(32) NOT NULL,
    patient_id BIGINT,
    patient_name VARCHAR(64),
    department VARCHAR(64),
    doctor_id BIGINT,
    doctor_name VARCHAR(64),
    appointment_date DATE,
    time_slot VARCHAR(32),
    status TINYINT DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS payment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    payment_no VARCHAR(32) NOT NULL,
    patient_id BIGINT,
    patient_name VARCHAR(64),
    item_name VARCHAR(64),
    item_type VARCHAR(32),
    department VARCHAR(64),
    doctor_name VARCHAR(64),
    amount DECIMAL(10,2),
    pay_method VARCHAR(32),
    status TINYINT DEFAULT 0,
    pay_time TIMESTAMP,
    advice VARCHAR(512),
    guide_tip VARCHAR(512),
    fee_breakdown LONGTEXT,
    voucher_no VARCHAR(64),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS medical_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_id BIGINT,
    patient_name VARCHAR(64),
    doctor_id BIGINT,
    doctor_name VARCHAR(64),
    department VARCHAR(64),
    diagnosis VARCHAR(255),
    treatment VARCHAR(512),
    visit_time TIMESTAMP,
    status TINYINT DEFAULT 2,
    revision_status TINYINT DEFAULT 0,
    revisions LONGTEXT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS drug (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    drug_code VARCHAR(32),
    drug_name VARCHAR(128),
    specification VARCHAR(64),
    manufacturer VARCHAR(128),
    unit VARCHAR(16),
    price DECIMAL(10,2),
    drug_type VARCHAR(32),
    risk_level VARCHAR(32),
    archived TINYINT DEFAULT 0,
    status TINYINT DEFAULT 1,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS schedule (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    doctor_id BIGINT,
    doctor_name VARCHAR(64),
    department VARCHAR(64),
    shift_date DATE,
    shift_type VARCHAR(32),
    status TINYINT DEFAULT 1,
    remark VARCHAR(255),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS medical_service (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    service_name VARCHAR(128),
    category VARCHAR(32),
    department VARCHAR(64),
    price DECIMAL(10,2),
    duration INT,
    status TINYINT DEFAULT 1,
    fee_item VARCHAR(32),
    description VARCHAR(512),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS bed (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    bed_no VARCHAR(32),
    ward VARCHAR(64),
    bed_type VARCHAR(32),
    department VARCHAR(64),
    status TINYINT DEFAULT 0,
    patient_name VARCHAR(64),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS finance_account (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    account_no VARCHAR(32),
    account_name VARCHAR(128),
    account_type VARCHAR(32),
    balance DECIMAL(12,2) DEFAULT 0,
    bank VARCHAR(64),
    overdraft TINYINT DEFAULT 0,
    warn_threshold DECIMAL(12,2),
    archived TINYINT DEFAULT 0,
    status TINYINT DEFAULT 1,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS income_expense (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    record_no VARCHAR(32),
    record_type VARCHAR(16),
    category VARCHAR(64),
    amount DECIMAL(12,2),
    department VARCHAR(64),
    operator VARCHAR(64),
    account_id BIGINT,
    source_module VARCHAR(64),
    source_id VARCHAR(64),
    record_time TIMESTAMP,
    remark VARCHAR(255),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS reimbursement (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    apply_no VARCHAR(32),
    applicant VARCHAR(64),
    department VARCHAR(64),
    amount DECIMAL(12,2),
    reason VARCHAR(255),
    status TINYINT DEFAULT 0,
    apply_time TIMESTAMP,
    workflow_json VARCHAR(2000),
    attachments_json VARCHAR(500),
    approve_time TIMESTAMP,
    approve_operator VARCHAR(64),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS settlement (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    settlement_no VARCHAR(32),
    patient_name VARCHAR(64),
    total_amount DECIMAL(12,2),
    paid_amount DECIMAL(12,2),
    status TINYINT DEFAULT 0,
    settle_time TIMESTAMP,
    items_json VARCHAR(2000),
    invoice_no VARCHAR(64),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS procurement (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_no VARCHAR(32),
    drug_name VARCHAR(128),
    quantity INT,
    unit_price DECIMAL(10,2),
    supplier VARCHAR(128),
    phase TINYINT DEFAULT 0,
    urgent TINYINT DEFAULT 0,
    logistics_no VARCHAR(64),
    receipt_note VARCHAR(255),
    status TINYINT DEFAULT 0,
    order_time TIMESTAMP,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS inventory (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    drug_id BIGINT,
    drug_code VARCHAR(32),
    drug_name VARCHAR(128),
    quantity INT,
    min_stock INT DEFAULT 50,
    batch_no VARCHAR(32),
    expiry_date DATE,
    warehouse VARCHAR(64),
    status TINYINT DEFAULT 1,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS dispensing (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    dispensing_no VARCHAR(32),
    prescription_no VARCHAR(32),
    barcode VARCHAR(64),
    patient_name VARCHAR(64),
    drug_name VARCHAR(128),
    quantity INT,
    pharmacist VARCHAR(64),
    priority VARCHAR(16) DEFAULT '门诊',
    status TINYINT DEFAULT 0,
    dispensing_time TIMESTAMP,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS sys_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(64) NOT NULL,
    code VARCHAR(32),
    description VARCHAR(255),
    status TINYINT DEFAULT 1,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS sys_role_menu (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_code VARCHAR(32) NOT NULL,
    menu_id BIGINT NOT NULL
);

CREATE TABLE IF NOT EXISTS sys_menu (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    parent_id BIGINT DEFAULT 0,
    name VARCHAR(64) NOT NULL,
    path VARCHAR(128),
    icon VARCHAR(64),
    sort INT DEFAULT 0,
    status TINYINT DEFAULT 1,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS sys_dict (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    dict_type VARCHAR(64),
    dict_label VARCHAR(64),
    dict_value VARCHAR(64),
    sort INT DEFAULT 0,
    status TINYINT DEFAULT 1,
    remark VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS sys_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    config_key VARCHAR(64) NOT NULL,
    config_value VARCHAR(255),
    description VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS sys_operation_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    module VARCHAR(64),
    action VARCHAR(64),
    operator VARCHAR(64),
    ip VARCHAR(64),
    status TINYINT DEFAULT 1,
    source VARCHAR(16) DEFAULT 'backend',
    client VARCHAR(16),
    path VARCHAR(255),
    detail VARCHAR(512),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS sys_login_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(64),
    ip VARCHAR(64),
    status TINYINT DEFAULT 1,
    message VARCHAR(255),
    login_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);


-- 演示种子数据（与前端 Mock 对齐，含深度操作字段）

INSERT INTO sys_user (username, password, name, role, role_label, department) VALUES
('admin', '123456', '系统管理员', 'admin', '管理员', NULL),
('doctor', '123456', '张医生', 'doctor', '医生', '内科'),
('nurse', '123456', '李护士', 'nurse', '护士', '内科'),
('patient', '123456', '张三', 'patient', '患者', NULL);

INSERT INTO sys_department (name, parent_id, sort, status, description) VALUES
('内科', 0, 1, 1, '内科诊疗'),
('外科', 0, 2, 1, '外科诊疗'),
('儿科', 0, 3, 1, '儿科诊疗'),
('骨科', 0, 4, 1, '骨科诊疗'),
('眼科', 0, 5, 1, '眼科诊疗'),
('皮肤科', 0, 6, 1, '皮肤科诊疗'),
('口腔科', 0, 7, 1, '口腔科诊疗');

INSERT INTO patient (patient_no, name, gender, age, phone, id_card, card_no, address, allergy_history, chronic_disease, status, user_id) VALUES
('P2026001286', '张三', 1, 36, '13800138000', '110101199001011234', 'P2026001286', '北京市朝阳区', '无', '无', 0, 4);

INSERT INTO doctor (doctor_no, name, gender, department, title, specialty, phone, status) VALUES
('D000001', '张明', 1, '内科', '主任医师', '心血管', '13900001111', 1),
('D000002', '王芳', 2, '儿科', '副主任医师', '儿科保健', '13900002222', 1),
('D000003', '李强', 1, '外科', '主治医师', '普外', '13900003333', 1),
('D000004', '赵敏', 2, '骨科', '主任医师', '骨科手术', '13900004444', 1),
('D000005', '刘洋', 1, '眼科', '主治医师', '眼科疾病', '13900005555', 1);

INSERT INTO notice (title, content, notice_type, publisher, status, publish_time) VALUES
('夏季防暑健康提示', '近期高温天气，请患者注意防暑降温，适量饮水。', '公告', '院办', 1, CURRENT_TIMESTAMP),
('门诊时间调整通知', '7月起周六上午增设专家门诊，请提前预约。', '通知', '门诊部', 1, CURRENT_TIMESTAMP),
('医保政策更新', '本市医保报销比例有所调整，详情请咨询收费窗口。', '通知', '医保办', 1, CURRENT_TIMESTAMP);

INSERT INTO register_order (register_no, patient_id, patient_name, department, doctor_id, doctor_name, register_type, fee, status, register_time) VALUES
('GH20260615001', 1, '张三', '内科', 1, '张明', '普通号', 10.00, 2, '2026-06-15 08:30:00');

INSERT INTO appointment (appointment_no, patient_id, patient_name, department, doctor_id, doctor_name, appointment_date, time_slot, status) VALUES
('YY20260618001', 1, '张三', '内科', 1, '张明', CURDATE(), '09:00-10:00', 0);

INSERT INTO payment (payment_no, patient_id, patient_name, item_name, item_type, department, doctor_name, amount, pay_method, status, pay_time, advice, guide_tip, fee_breakdown) VALUES
('JF20260615001', 1, '张三', '挂号费', 'register', '内科', '张明', 10.00, '微信', 1, '2026-06-15 08:32:00', '请按时到院就诊', '缴费完成后请前往对应科室候诊', '[{"name":"挂号费","amount":10}]'),
('JF20260620002', 1, '张三', '检查费', 'check', '内科', '张明', 280.00, '', 0, NULL, '请空腹检查', '缴费后前往检验科', '[{"name":"血常规","amount":35},{"name":"C反应蛋白","amount":45},{"name":"胸部CT平扫","amount":200}]'),
('JF20260620003', 1, '张三', '药品费', 'medicine', '内科', '张明', 156.50, '支付宝', 1, '2026-06-20 15:20:00', '请按医嘱服药', '取药请前往门诊药房1号窗口', '[{"name":"阿莫西林胶囊","amount":28.5}]');

INSERT INTO medical_record (patient_id, patient_name, doctor_id, doctor_name, department, diagnosis, treatment, visit_time, status, revision_status, revisions) VALUES
(1, '张三', 1, '张明', '内科', '上呼吸道感染', '抗病毒治疗，多休息', '2026-06-15 09:30:00', 2, 0, '[]'),
(1, '张三', 2, '王芳', '儿科', '健康查体', '常规保健指导', '2026-05-20 14:00:00', 2, 0, '[]');

INSERT INTO drug (drug_code, drug_name, specification, manufacturer, unit, price, drug_type, risk_level, archived, status) VALUES
('DR000001', '阿莫西林', '0.25g*24粒', '华北制药', '盒', 28.50, '处方药', '普通', 0, 1),
('DR000002', '布洛芬', '100ml', '扬子江药业', '瓶', 32.00, 'OTC', '普通', 0, 1),
('DR000003', '头孢克肟', '0.1g*12粒', '石药集团', '盒', 45.00, '处方药', '普通', 0, 1);

INSERT INTO schedule (doctor_id, doctor_name, department, shift_date, shift_type, status) VALUES
(1, '张明', '内科', CURDATE(), '早班', 1),
(1, '张明', '内科', DATE_ADD(CURDATE(), INTERVAL 1 DAY), '早班', 1),
(2, '王芳', '儿科', CURDATE(), '中班', 1);

INSERT INTO medical_service (service_name, category, department, price, duration, status, fee_item) VALUES
('门诊挂号', '其他', '内科', 10.00, 15, 1, '挂号费'),
('CT检查', '检查', '内科', 200.00, 30, 1, '检查费'),
('专家会诊', '治疗', '外科', 300.00, 45, 0, '检查费');

INSERT INTO bed (bed_no, ward, bed_type, department, status) VALUES
('A101', '内科一病区', '普通床位', '内科', 0),
('A102', '内科一病区', 'VIP床位', '内科', 1);

INSERT INTO finance_account (account_no, account_name, account_type, balance, bank, overdraft, warn_threshold, archived, status) VALUES
('AC20260001', '门诊收入账户', '收入', 98560.50, '工商银行', 0, 20000.00, 0, 1),
('AC20260002', '药品采购账户', '支出', 50000.00, '建设银行', 0, 10000.00, 0, 1),
('AC20260003', '住院收入账户', '收入', 120000.00, '农业银行', 1, 30000.00, 0, 1);

INSERT INTO income_expense (record_no, record_type, category, amount, department, operator, account_id, source_module, source_id, record_time) VALUES
('IE202606001', 'income', '挂号收入', 1500.00, '门诊部', 'admin', 1, 'register', 'GH20260615001', CURRENT_TIMESTAMP),
('IE202606002', 'expense', '药品采购', 800.00, '药房', 'admin', 2, 'procurement', 'PO202606001', CURRENT_TIMESTAMP),
('IE202606003', 'income', '检查收入', 2800.00, '门诊部', 'admin', 1, 'payment', 'JF20260620002', CURRENT_TIMESTAMP),
('IE202606004', 'income', '住院结算', 5200.00, '住院部', 'admin', 3, 'settlement', 'ST202606001', CURRENT_TIMESTAMP);

INSERT INTO reimbursement (apply_no, applicant, department, amount, reason, status, apply_time, workflow_json, attachments_json) VALUES
('RB202606001', '张医生', '内科', 500.00, '学术会议差旅', 0, CURRENT_TIMESTAMP,
 '[{"node":"提交申请","operator":"张医生","status":"done"},{"node":"科室审批","operator":"科室主任","status":"pending"},{"node":"财务打款","operator":"财务部","status":"pending"}]',
 '["发票扫描件.pdf"]');

INSERT INTO settlement (settlement_no, patient_name, total_amount, paid_amount, status, items_json) VALUES
('ST202606001', '张三', 500.00, 300.00, 0,
 '[{"name":"诊疗费","amount":500},{"name":"已付","amount":300}]');

INSERT INTO procurement (order_no, drug_name, quantity, unit_price, supplier, phase, urgent, logistics_no, receipt_note, status, order_time) VALUES
('PO202606001', '阿莫西林', 500, 25.00, '华北制药', 1, 0, 'SF1234567890', '首批到货', 1, CURRENT_TIMESTAMP),
('PO202606002', '布洛芬', 300, 18.00, '国药控股', 0, 1, '', '库存告警自动生成', 0, CURRENT_TIMESTAMP);

INSERT INTO inventory (drug_id, drug_code, drug_name, quantity, min_stock, batch_no, expiry_date, warehouse, status) VALUES
(1, 'DR000001', '阿莫西林', 800, 100, 'BN202601', DATE_ADD(CURDATE(), INTERVAL 12 MONTH), '中心药库', 1),
(2, 'DR000002', '布洛芬', 45, 50, 'BN202602', DATE_ADD(CURDATE(), INTERVAL 18 MONTH), '中心药库', 1);

INSERT INTO dispensing (dispensing_no, prescription_no, barcode, patient_name, drug_name, quantity, pharmacist, priority, status, create_time) VALUES
('DP202606001', 'RX202606001', 'BC202606001', '张三', '阿莫西林', 2, '王药师', '门诊', 0, CURRENT_TIMESTAMP),
('DP202606002', 'RX202606002', 'BC202606002', '李四', '布洛芬', 1, '王药师', '急诊', 0, CURRENT_TIMESTAMP),
('DP202606003', 'RX202606003', 'BC202606003', '王五', '头孢克肟', 3, '李药师', '住院', 2, CURRENT_TIMESTAMP);

INSERT INTO sys_role (name, code, description, status) VALUES
('系统管理员', 'admin', '全部权限', 1),
('医生', 'doctor', '诊疗相关', 1),
('护士', 'nurse', '护理相关', 1);

INSERT INTO sys_config (config_key, config_value, description) VALUES
('hospital_name', '东软云医院', '医院名称'),
('register_fee', '10', '普通挂号费'),
('expert_fee', '50', '专家挂号费'),
('appointment_days', '7', '预约天数'),
('session_timeout', '30', '会话超时');

INSERT INTO sys_operation_log (module, action, operator, ip, status, source) VALUES
('患者管理', '查询列表', 'admin', '127.0.0.1', 1, 'backend');

INSERT INTO sys_login_log (username, ip, status, message) VALUES
('admin', '127.0.0.1', 1, '登录成功');

INSERT INTO sys_menu (id, parent_id, name, path, icon, sort, status) VALUES
(1, 0, '工作台', '/dashboard', 'Odometer', 1, 1),
(2, 0, '患者管理', '/patient', 'User', 2, 1),
(21, 2, '患者信息管理', '/patient/info', NULL, 1, 1),
(22, 2, '医生添加患者', '/patient/add', NULL, 2, 1),
(23, 2, '患者查询', '/patient/search', NULL, 3, 1),
(24, 2, '医生开始就诊', '/patient/consultation', NULL, 4, 1),
(3, 0, '人事管理', '/hr', 'Avatar', 3, 1),
(31, 3, '医生信息管理', '/hr/doctor', NULL, 1, 1),
(32, 3, '排班管理', '/hr/schedule', NULL, 2, 1),
(33, 3, '诊疗记录管理', '/hr/record', NULL, 3, 1),
(34, 3, '医疗服务管理', '/hr/service', NULL, 4, 1),
(4, 0, '药房管理', '/pharmacy', 'Goods', 4, 1),
(41, 4, '药品信息管理', '/pharmacy/drug', NULL, 1, 1),
(42, 4, '采购管理', '/pharmacy/procurement', NULL, 2, 1),
(43, 4, '库存管理', '/pharmacy/inventory', NULL, 3, 1),
(44, 4, '配药管理', '/pharmacy/dispensing', NULL, 4, 1),
(5, 0, '财务管理', '/finance', 'Wallet', 5, 1),
(51, 5, '财务账户', '/finance/info', NULL, 1, 1),
(52, 5, '收支管理', '/finance/income-expense', NULL, 2, 1),
(53, 5, '报销管理', '/finance/reimbursement', NULL, 3, 1),
(54, 5, '结算管理', '/finance/settlement', NULL, 4, 1),
(6, 0, '统计分析', '/statistics', 'DataAnalysis', 6, 1),
(61, 6, '数据概览', '/statistics/overview', NULL, 1, 1),
(62, 6, '数据分析', '/statistics/analysis', NULL, 2, 1),
(63, 6, '报表中心', '/statistics/reports', NULL, 3, 1),
(64, 6, '决策支持', '/statistics/decision', NULL, 4, 1),
(7, 0, '业务管理', '/business', 'Tickets', 7, 1),
(71, 7, '挂号管理', '/business/register', NULL, 1, 1),
(72, 7, '预约管理', '/business/appointment', NULL, 2, 1),
(73, 7, '缴费管理', '/business/payment', NULL, 3, 1),
(74, 7, '床位管理', '/business/bed', NULL, 4, 1),
(75, 7, '公告管理', '/business/notice', NULL, 5, 1),
(10, 0, '系统管理', '/admin', 'Setting', 10, 1),
(101, 10, '用户管理', '/admin/user', NULL, 1, 1),
(102, 10, '角色管理', '/admin/role', NULL, 2, 1),
(103, 10, '科室管理', '/admin/department', NULL, 3, 1),
(104, 10, '菜单权限', '/admin/menu', NULL, 4, 1),
(105, 10, '数据字典', '/admin/dict', NULL, 5, 1),
(106, 10, '系统配置', '/admin/config', NULL, 6, 1),
(107, 10, '操作日志', '/admin/operation-log', NULL, 7, 1),
(108, 10, '登录日志', '/admin/login-log', NULL, 8, 1);

INSERT INTO sys_dict (dict_type, dict_label, dict_value, sort, status, remark) VALUES
('gender', '男', '1', 1, 1, '性别'),
('gender', '女', '2', 2, 1, '性别'),
('register_type', '普通号', 'normal', 1, 1, '挂号类型'),
('register_type', '专家号', 'expert', 2, 1, '挂号类型'),
('pay_method', '微信', 'wechat', 1, 1, '支付方式'),
('pay_method', '支付宝', 'alipay', 2, 1, '支付方式'),
('pay_method', '现金', 'cash', 3, 1, '支付方式'),
('bed_type', '普通床位', 'normal', 1, 1, '床位类型'),
('bed_type', 'VIP床位', 'vip', 2, 1, '床位类型'),
('notice_type', '公告', 'notice', 1, 1, '公告类型'),
('notice_type', '通知', 'info', 2, 1, '公告类型');

INSERT INTO sys_role_menu (role_code, menu_id) VALUES
('admin', 1), ('admin', 2), ('admin', 21), ('admin', 22), ('admin', 23), ('admin', 24),
('admin', 3), ('admin', 31), ('admin', 32), ('admin', 33), ('admin', 34),
('admin', 4), ('admin', 41), ('admin', 42), ('admin', 43), ('admin', 44),
('admin', 5), ('admin', 51), ('admin', 52), ('admin', 53), ('admin', 54),
('admin', 6), ('admin', 61), ('admin', 62), ('admin', 63), ('admin', 64),
('admin', 7), ('admin', 71), ('admin', 72), ('admin', 73), ('admin', 74), ('admin', 75),
('admin', 10), ('admin', 101), ('admin', 102), ('admin', 103), ('admin', 104),
('admin', 105), ('admin', 106), ('admin', 107), ('admin', 108),
('doctor', 2), ('doctor', 21), ('doctor', 22), ('doctor', 23), ('doctor', 24),
('doctor', 3), ('doctor', 31), ('doctor', 32), ('doctor', 33), ('doctor', 34),
('nurse', 2), ('nurse', 21), ('nurse', 23), ('nurse', 7), ('nurse', 71), ('nurse', 72);

-- ========== 演示数据充盈（全页面有内容） ==========
-- 子科室
INSERT INTO sys_department (name, parent_id, sort, status, description) VALUES
('心血管内科', 1, 1, 1, '内科-心血管'),
('呼吸内科', 1, 2, 1, '内科-呼吸'),
('普外科', 2, 1, 1, '外科-普外'),
('创伤骨科', 4, 1, 1, '骨科-创伤');

INSERT INTO sys_user (username, password, name, role, role_label, department) VALUES
('pharmacist', '123456', '王药师', 'nurse', '药师', '药房'),
('finance01', '123456', '李会计', 'admin', '财务', '财务部');

INSERT INTO patient (patient_no, name, gender, age, phone, id_card, card_no, address, allergy_history, chronic_disease, status, user_id) VALUES
('P2026001287', '李四', 1, 28, '13900139001', '110101199502021234', 'P2026001287', '上海市浦东新区', '青霉素过敏', '无', 0, NULL),
('P2026001288', '王五', 2, 45, '13900139002', '110101198003031234', 'P2026001288', '广州市天河区', '无', '高血压', 1, NULL),
('P2026001289', '赵六', 1, 62, '13900139003', '110101196505051234', 'P2026001289', '深圳市南山区', '无', '糖尿病', 2, NULL),
('P2026001290', '孙七', 2, 8, '13900139004', '110101201801011234', 'P2026001290', '杭州市西湖区', '无', '无', 0, NULL),
('P2026001291', '周八', 1, 33, '13900139005', '110101199301011234', 'P2026001291', '成都市武侯区', '海鲜过敏', '无', 0, NULL),
('P2026001292', '吴九', 2, 55, '13900139006', '110101197002021234', 'P2026001292', '武汉市江汉区', '无', '冠心病', 1, NULL),
('P2026001293', '郑十', 1, 19, '13900139007', '110101200701011234', 'P2026001293', '南京市鼓楼区', '无', '无', 0, NULL);

INSERT INTO doctor (doctor_no, name, gender, department, title, specialty, phone, status) VALUES
('D000006', '陈静', 2, '皮肤科', '副主任医师', '皮肤病', '13900006666', 1),
('D000007', '杨帆', 1, '口腔科', '主治医师', '口腔修复', '13900007777', 1),
('D000008', '黄磊', 1, '外科', '主任医师', '肝胆外科', '13900008888', 1),
('D000009', '林娜', 2, '儿科', '主治医师', '小儿呼吸', '13900009999', 1),
('D000010', '马超', 1, '骨科', '副主任医师', '脊柱外科', '13900010001', 1);

INSERT INTO notice (title, content, notice_type, publisher, status, publish_time) VALUES
('新院区开放试运行', '东软云医院西院区将于8月1日起试运行，首批开放内科、外科门诊。', '公告', '院办', 1, DATE_SUB(NOW(), INTERVAL 3 DAY)),
('体检套餐优惠活动', '6月28日-7月31日，基础体检套餐8折优惠，详情请咨询健康管理中心。', '通知', '健康管理中心', 1, DATE_SUB(NOW(), INTERVAL 1 DAY)),
('停诊通知', '因设备维护，6月30日下午放射科暂停接诊，请已预约患者改约。', '通知', '门诊部', 1, NOW());

INSERT INTO register_order (register_no, patient_id, patient_name, department, doctor_id, doctor_name, register_type, fee, status, register_time) VALUES
('GH20260620001', 1, '张三', '外科', 3, '李强', '专家号', 50.00, 0, DATE_SUB(NOW(), INTERVAL 2 DAY)),
('GH20260622001', 1, '张三', '儿科', 2, '王芳', '普通号', 10.00, 1, DATE_SUB(NOW(), INTERVAL 1 DAY)),
('GH20260628001', 1, '张三', '骨科', 4, '赵敏', '普通号', 10.00, 3, NOW()),
('GH20260628002', 2, '李四', '内科', 1, '张明', '普通号', 10.00, 0, NOW()),
('GH20260628003', 3, '王五', '骨科', 4, '赵敏', '专家号', 50.00, 1, DATE_SUB(NOW(), INTERVAL 1 HOUR)),
('GH20260628004', 5, '孙七', '儿科', 2, '王芳', '普通号', 10.00, 2, DATE_SUB(NOW(), INTERVAL 3 HOUR)),
('GH20260628005', 6, '周八', '皮肤科', 6, '陈静', '普通号', 10.00, 3, DATE_SUB(NOW(), INTERVAL 5 HOUR));

INSERT INTO appointment (appointment_no, patient_id, patient_name, department, doctor_id, doctor_name, appointment_date, time_slot, status) VALUES
('YY20260625001', 1, '张三', '眼科', 5, '刘洋', DATE_ADD(CURDATE(), INTERVAL 2 DAY), '14:00-15:00', 0),
('YY20260626001', 1, '张三', '皮肤科', 6, '陈静', DATE_ADD(CURDATE(), INTERVAL 3 DAY), '09:00-10:00', 1),
('YY20260610001', 1, '张三', '内科', 1, '张明', DATE_SUB(CURDATE(), INTERVAL 5 DAY), '10:00-11:00', 2),
('YY20260628002', 2, '李四', '外科', 3, '李强', DATE_ADD(CURDATE(), INTERVAL 1 DAY), '08:00-09:00', 0),
('YY20260628003', 3, '王五', '骨科', 10, '马超', DATE_ADD(CURDATE(), INTERVAL 4 DAY), '15:00-16:00', 1),
('YY20260628004', 7, '郑十', '口腔科', 7, '杨帆', DATE_ADD(CURDATE(), INTERVAL 5 DAY), '11:00-12:00', 0);

INSERT INTO payment (payment_no, patient_id, patient_name, item_name, item_type, department, doctor_name, amount, pay_method, status, pay_time, advice, guide_tip, fee_breakdown) VALUES
('JF20260618001', 1, '张三', '住院押金', 'deposit', '内科', '张明', 2000.00, '微信', 1, DATE_SUB(NOW(), INTERVAL 4 DAY), '住院部办理入院', '请携带身份证', '[{"name":"住院押金","amount":2000}]'),
('JF20260624001', 1, '张三', 'B超检查', 'check', '外科', '李强', 120.00, '', 0, NULL, '检查前禁食8小时', '缴费后前往超声科', '[{"name":"腹部B超","amount":120}]'),
('JF20260627001', 1, '张三', '专家挂号费', 'register', '骨科', '赵敏', 50.00, '支付宝', 2, DATE_SUB(NOW(), INTERVAL 1 DAY), '已退款', '退款3个工作日到账', '[{"name":"专家挂号费","amount":50}]'),
('JF20260628001', 2, '李四', '血常规', 'check', '内科', '张明', 35.00, '微信', 1, NOW(), '空腹采血', '检验科2号窗口', '[{"name":"血常规","amount":35}]'),
('JF20260628002', 3, '王五', 'MRI检查', 'check', '骨科', '赵敏', 680.00, '', 0, NULL, '去除金属物品', '影像科预约', '[{"name":"MRI","amount":680}]'),
('JF20260628003', 5, '孙七', '疫苗接种', 'medicine', '儿科', '王芳', 168.00, '支付宝', 1, DATE_SUB(NOW(), INTERVAL 2 HOUR), '留观30分钟', '儿科接种室', '[{"name":"流感疫苗","amount":168}]');

INSERT INTO medical_record (patient_id, patient_name, doctor_id, doctor_name, department, diagnosis, treatment, visit_time, status, revision_status, revisions) VALUES
(1, '张三', 1, '张明', '内科', '急性支气管炎', '止咳化痰，注意休息', DATE_SUB(NOW(), INTERVAL 10 DAY), 2, 0, '[]'),
(1, '张三', 3, '李强', '外科', '阑尾炎术后复查', '伤口恢复良好', DATE_SUB(NOW(), INTERVAL 30 DAY), 2, 1, '[{"time":"2026-05-20","content":"初诊记录"}]'),
(2, '李四', 1, '张明', '内科', '高血压初诊', '降压药物+低盐饮食', DATE_SUB(NOW(), INTERVAL 3 DAY), 1, 0, '[]'),
(3, '王五', 4, '赵敏', '骨科', '腰椎间盘突出', '理疗+药物', DATE_SUB(NOW(), INTERVAL 7 DAY), 2, 0, '[]'),
(4, '赵六', 2, '王芳', '儿科', '小儿感冒', '对症处理', DATE_SUB(NOW(), INTERVAL 2 DAY), 0, 0, '[]');

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

INSERT INTO sys_operation_log (module, action, operator, ip, status, source) VALUES
('挂号管理', '新增挂号', 'nurse', '127.0.0.1', 1, 'backend'),
('预约管理', '确认预约', 'admin', '127.0.0.1', 1, 'backend'),
('缴费管理', '发起退款', 'admin', '127.0.0.1', 1, 'backend'),
('药房管理', '完成配药', 'pharmacist', '127.0.0.1', 1, 'backend'),
('财务管理', '审批报销', 'admin', '127.0.0.1', 1, 'backend'),
('患者管理', '开始就诊', 'doctor', '127.0.0.1', 1, 'backend'),
('系统管理', '修改配置', 'admin', '127.0.0.1', 1, 'backend'),
('人事管理', '导入医生', 'admin', '127.0.0.1', 1, 'backend'),
('库存管理', '库存调整', 'pharmacist', '127.0.0.1', 1, 'backend'),
('统计分析', '导出报表', 'admin', '127.0.0.1', 1, 'backend');

INSERT INTO sys_login_log (username, ip, status, message) VALUES
('doctor', '127.0.0.1', 1, '登录成功'),
('nurse', '127.0.0.1', 1, '登录成功'),
('patient', '127.0.0.1', 1, '登录成功'),
('admin', '192.168.1.100', 1, '登录成功'),
('admin', '192.168.1.100', 0, '密码错误'),
('doctor', '10.0.0.5', 1, '登录成功'),
('admin', '127.0.0.1', 1, '登录成功');

SET FOREIGN_KEY_CHECKS = 1;
-- Default: admin/123456, patient 13800138000/123456
