-- Liquibase database migration script
CREATE TABLE IF NOT EXISTS monitors
(
    id                      VARCHAR(120) PRIMARY KEY,
    timeout                 SMALLINT                                           NOT NULL,
    alert_email             VARCHAR(150)                                       NOT NULL,
    status                  VARCHAR(30)                                        NOT NULL DEFAULT 'ACTIVE',
    next_expected_heartbeat TIMESTAMP WITH TIME ZONE,
    created_at              TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at              TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- Optimize background sweeper performance with an index on status and heartbeat
CREATE INDEX IF NOT EXISTS idx_monitors_sweeper
    ON monitors (status, next_expected_heartbeat)
    WHERE status = 'ACTIVE';