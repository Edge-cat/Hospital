ALTER TABLE payment
  ADD COLUMN register_id BIGINT NULL AFTER voucher_no;
