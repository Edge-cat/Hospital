-- 修复 PowerShell 管道导入导致的中文乱码（?? / 0x3F）
-- 用法: execution/scripts/import-sql-utf8.ps1 -SqlFile execution/database/hospital_his_seed_fixup.sql

USE hospital_his;
SET NAMES utf8mb4;

-- 删除乱码增量数据（按业务键，保留 id=1 张三等原始种子）
DELETE FROM dispensing WHERE dispensing_no IN ('DP202606004','DP202606005','DP202606006','DP202606007');
DELETE FROM inventory WHERE drug_id IN (3,4,5,6,7) AND batch_no IN ('BN202603','BN202604','BN202605','BN202606','BN202607');
DELETE FROM procurement WHERE order_no IN ('PO202606003','PO202606004','PO202606005');
DELETE FROM settlement WHERE settlement_no IN ('ST202606002','ST202606003');
DELETE FROM reimbursement WHERE apply_no IN ('RB202606002','RB202606003');
DELETE FROM income_expense WHERE record_no IN ('IE202606005','IE202606006','IE202606007','IE202606008');
DELETE FROM bed WHERE bed_no IN ('A103','A104','B201','B202','C301','D401','E501');
DELETE FROM medical_service WHERE service_name IN ('血常规检查','疫苗接种','康复理疗','健康体检套餐','急诊留观');
DELETE FROM schedule WHERE doctor_id IN (6,7,8) OR (doctor_id = 1 AND shift_type = '晚班');
DELETE FROM drug WHERE drug_code IN ('DR000004','DR000005','DR000006','DR000007');
DELETE FROM medical_record WHERE patient_id IN (2,3,4) AND visit_time >= '2026-06-01';
DELETE FROM payment WHERE payment_no IN ('JF20260618001','JF20260624001','JF20260627001','JF20260628001','JF20260628002','JF20260628003');
DELETE FROM appointment WHERE appointment_no IN ('YY20260625001','YY20260626001','YY20260610001','YY20260628002','YY20260628003','YY20260628004');
DELETE FROM register_order WHERE register_no IN ('GH20260620001','GH20260622001','GH20260628001','GH20260628002','GH20260628003','GH20260628004','GH20260628005');
DELETE FROM notice WHERE title LIKE '%院区%' OR title LIKE '%体检%' OR title LIKE '%停诊%';
DELETE FROM doctor WHERE doctor_no IN ('D000006','D000007','D000008','D000009','D000010');
DELETE FROM patient WHERE patient_no IN ('P2026001287','P2026001288','P2026001289','P2026001290','P2026001291','P2026001292','P2026001293');
DELETE FROM sys_user WHERE username IN ('pharmacist','finance01');
DELETE FROM sys_department WHERE name IN ('心血管内科','呼吸内科','普外科','创伤骨科');
DELETE FROM sys_dict WHERE dict_type IN ('appointment_status','payment_status','register_status');
DELETE FROM sys_operation_log WHERE module IN ('挂号管理','预约管理','缴费管理','药房管理','财务管理','患者管理','系统管理','人事管理','库存管理','统计分析');
DELETE FROM sys_login_log WHERE username IN ('doctor','nurse','patient') OR ip IN ('192.168.1.100','10.0.0.5');
