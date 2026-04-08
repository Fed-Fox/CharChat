CREATE TABLE IF NOT EXISTS users (
    id VARCHAR(64),
    wsid VARCHAR(64),
    mail VARCHAR,
    tag VARCHAR,
    displayName VARCHAR,
    password VARCHAR,
    PRIMARY KEY (id, wsid, mail, tag)
);

CREATE TABLE IF NOT EXISTS chats (
    id VARCHAR(64) PRIMARY KEY,
    starter VARCHAR(64),
    ender VARCHAR(64)
);

CREATE TABLE IF NOT EXISTS messages (
    id VARCHAR(64) PRIMARY KEY,
    sender VARCHAR(64),
    chat VARCHAR(64),
    content VARCHAR,
    time TIMESTAMP,
    type INTEGER
);