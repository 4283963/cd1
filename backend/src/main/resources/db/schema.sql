-- 流浪猫喂养管理系统 数据库初始化脚本
-- PostgreSQL

-- 创建数据库
-- CREATE DATABASE catfeeder;
-- \c catfeeder;

-- 猫咪表
CREATE TABLE IF NOT EXISTS cats (
    id BIGSERIAL PRIMARY KEY,
    cat_code VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    fur_color VARCHAR(50),
    fur_pattern VARCHAR(50),
    body_type VARCHAR(50),
    eye_color VARCHAR(50),
    gender VARCHAR(20),
    estimated_age INTEGER,
    description VARCHAR(500),
    avatar_url VARCHAR(500),
    is_neutered BOOLEAN NOT NULL DEFAULT false,
    is_new BOOLEAN NOT NULL DEFAULT true,
    first_seen_time TIMESTAMP,
    last_seen_time TIMESTAMP,
    visit_count INTEGER NOT NULL DEFAULT 0,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 喂养机表
CREATE TABLE IF NOT EXISTS feeders (
    id BIGSERIAL PRIMARY KEY,
    feeder_code VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    location VARCHAR(200),
    longitude DECIMAL(10, 6),
    latitude DECIMAL(10, 6),
    food_capacity INTEGER,
    current_food_level INTEGER,
    water_capacity INTEGER,
    current_water_level INTEGER,
    battery_level INTEGER,
    status VARCHAR(20),
    food_alert BOOLEAN NOT NULL DEFAULT false,
    water_alert BOOLEAN NOT NULL DEFAULT false,
    last_heartbeat TIMESTAMP,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 猫咪抓拍记录表
CREATE TABLE IF NOT EXISTS cat_captures (
    id BIGSERIAL PRIMARY KEY,
    feeder_code VARCHAR(50) NOT NULL,
    cat_id BIGINT,
    image_url VARCHAR(500) NOT NULL,
    fur_color VARCHAR(50),
    fur_pattern VARCHAR(50),
    body_type VARCHAR(50),
    eye_color VARCHAR(50),
    features VARCHAR(1000),
    is_new_cat BOOLEAN NOT NULL DEFAULT false,
    recognized BOOLEAN NOT NULL DEFAULT false,
    capture_time TIMESTAMP NOT NULL,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_cat_captures_feeder ON cat_captures(feeder_code);
CREATE INDEX IF NOT EXISTS idx_cat_captures_cat ON cat_captures(cat_id);
CREATE INDEX IF NOT EXISTS idx_cat_captures_time ON cat_captures(capture_time);

-- 传感器数据表
CREATE TABLE IF NOT EXISTS sensor_data (
    id BIGSERIAL PRIMARY KEY,
    feeder_code VARCHAR(50) NOT NULL,
    infrared_triggered BOOLEAN,
    food_level INTEGER,
    water_level INTEGER,
    temperature FLOAT,
    battery_level INTEGER,
    record_time TIMESTAMP NOT NULL,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_sensor_data_feeder ON sensor_data(feeder_code);
CREATE INDEX IF NOT EXISTS idx_sensor_data_time ON sensor_data(record_time);

-- 喂食记录表
CREATE TABLE IF NOT EXISTS feeding_records (
    id BIGSERIAL PRIMARY KEY,
    feeder_code VARCHAR(50) NOT NULL,
    cat_id BIGINT,
    amount INTEGER NOT NULL,
    feeding_time TIMESTAMP NOT NULL,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_feeding_records_feeder ON feeding_records(feeder_code);
CREATE INDEX IF NOT EXISTS idx_feeding_records_cat ON feeding_records(cat_id);
CREATE INDEX IF NOT EXISTS idx_feeding_records_time ON feeding_records(feeding_time);

-- 告警表
CREATE TABLE IF NOT EXISTS alerts (
    id BIGSERIAL PRIMARY KEY,
    type VARCHAR(50) NOT NULL,
    feeder_code VARCHAR(50),
    feeder_name VARCHAR(100),
    cat_id BIGINT,
    cat_name VARCHAR(100),
    message VARCHAR(500) NOT NULL,
    severity VARCHAR(20) NOT NULL,
    resolved BOOLEAN NOT NULL DEFAULT false,
    resolve_time TIMESTAMP,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_alerts_type ON alerts(type);
CREATE INDEX IF NOT EXISTS idx_alerts_resolved ON alerts(resolved);
CREATE INDEX IF NOT EXISTS idx_alerts_time ON alerts(create_time);
