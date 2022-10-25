-- liquibase formatted sql

-- changeset dnigrab:1
CREATE TABLE notificationtask (
    id REAL NOT NULL PRIMARY KEY,
    chat_Id REAL NOT NULL,
    task_Head TEXT NOT NULL,
    task_Text TEXT NOT NULL,
    notification_Time TIMESTAMP WITHOUT TIME ZONE
);
CREATE SEQUENCE IF NOT EXISTS hibernate_sequence;

