-- metadata 테이블 생성
CREATE TABLE metadata (
                          log_group VARCHAR(255) PRIMARY KEY,
                          timestamp TIMESTAMP
);

-- eventStream 테이블 생성
CREATE TABLE eventStream (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       event_name VARCHAR(255),
                       event_type VARCHAR(50),
                       timestamp TIMESTAMP,
                       logs TEXT
);

-- metadata 테이블에 데이터 삽입
INSERT INTO metadata (log_group, timestamp)
VALUES
    ('aws-cloudtrail-logs-058264524253-eba56a76', CURRENT_TIMESTAMP),
    ('aws-access-logs-groups', CURRENT_TIMESTAMP),
    ('aws-waf-logs-groups', CURRENT_TIMESTAMP);

