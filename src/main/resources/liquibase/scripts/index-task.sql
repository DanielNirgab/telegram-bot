-- liquibase formatted sql

-- changeset dnigrab:1
CREATE TABLE notificationtask (
    id BIGSERIAL NOT NULL PRIMARY KEY,
    chat_Id BIGSERIAL NOT NULL,
    task_Head TEXT NOT NULL,
    task_Text TEXT NOT NULL,
    notification_Time TIMESTAMP WITHOUT TIME ZONE
);
CREATE SEQUENCE IF NOT EXISTS hibernate_sequence;

