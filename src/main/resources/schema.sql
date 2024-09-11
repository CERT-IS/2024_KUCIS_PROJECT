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
--     ('aws-cloudtrail-logs-058264524253-eba56a76',  '2000-11-16 00:00:00'),
--     ('aws-access-logs-groups',  '2000-11-16 00:00:00'),
--     ('aws-waf-logs-groups',  '2000-11-16 00:00:00');
('aws-cloudtrail-logs-058264524253-eba56a76',  '2024-09-11 19:00:00'),
('aws-access-logs-groups', '2024-09-11 19:00:00'),
('aws-waf-logs-groups',  '2024-09-11 19:00:00');

