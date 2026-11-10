CREATE TABLE IF NOT EXISTS booking_item (
    item_id         BIGINT PRIMARY KEY,
    title           VARCHAR NOT NULL,
    preview_content VARCHAR(2000),
    status          VARCHAR NOT NULL DEFAULT 'AVAILABLE',
    borrowed_by     VARCHAR,
    version         BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT booking_item_status_chk CHECK (status IN ('AVAILABLE', 'LENT', 'INACTIVE'))
);
ALTER TABLE booking_item ADD COLUMN IF NOT EXISTS preview_content VARCHAR(2000);

CREATE TABLE IF NOT EXISTS user_account (
    user_id       VARCHAR PRIMARY KEY,
    username      VARCHAR NOT NULL UNIQUE,
    password_hash VARCHAR NOT NULL,
    roles         VARCHAR NOT NULL
);
