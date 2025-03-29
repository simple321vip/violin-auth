CREATE TABLE IF NOT EXISTS CUSTOMER(
    phone_number    CHAR(11) PRIMARY KEY
);

CREATE TABLE IF NOT EXISTS T_TENANT(
    tenant_id        VARCHAR(255) PRIMARY KEY,  -- 主键约束
    account         VARCHAR(255) NOT NULL,
    tel             VARCHAR(20) NOT NULL,
    authority       INT,
    avatar_url       VARCHAR(255),
    storage_account  VARCHAR(255)
);