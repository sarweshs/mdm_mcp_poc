CREATE TABLE rules (
    rule_id VARCHAR(50) PRIMARY KEY,
    domain VARCHAR(50) NOT NULL,
    condition TEXT NOT NULL,
    action TEXT NOT NULL
);

CREATE TABLE audit_logs (
    id SERIAL PRIMARY KEY,
    action VARCHAR(100) NOT NULL,
    user_id VARCHAR(50) NOT NULL,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);