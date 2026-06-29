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
    content CLOB,
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
    fee_breakdown CLOB,
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
    revisions CLOB,
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
