#!/bin/bash

set -e

source .env

if [ "$MYSQL_HOST" == "localhost" ]; then
    MYSQL_HOST="127.0.0.1"
fi

mysql -u "${MYSQL_USER}" -h "${MYSQL_HOST}" "-p${MYSQL_PASS}" << EOF
USE ${MYSQL_DB};

CREATE TABLE IF NOT EXISTS Users
(
    id           VARCHAR(36),
    created      BIGINT,
    name         VARCHAR(20),
    role         VARCHAR(10),
    winsAsBill   BIGINT,
    winsAsPlayer BIGINT,
    playtime     BIGINT,
    PRIMARY KEY (id)
);
CREATE TABLE IF NOT EXISTS UserKeys
(
    id          VARCHAR(36),
    userId      VARCHAR(36),
    created     BIGINT,
    expires     BIGINT,
    PRIMARY KEY (id, userId)
);
CREATE TABLE IF NOT EXISTS UserFriends
(
    fromId      VARCHAR(36),
    toId        VARCHAR(36),
    created     BIGINT,
    state       VARCHAR(10),
    PRIMARY KEY (fromId, toId)
);
CREATE TABLE IF NOT EXISTS UserAuthDetails
(
    userId      VARCHAR(36),
    updated     BIGINT,
    password    VARCHAR(100),
    email       VARCHAR(100),
    PRIMARY KEY (userId)
);
CREATE TABLE IF NOT EXISTS Games
(
    id          VARCHAR(36),
    name        VARCHAR(100),
    created     BIGINT,
    hostId      VARCHAR(36),
    config      MEDIUMTEXT,
    map         MEDIUMTEXT,
    PRIMARY KEY (id)
);
CREATE TABLE IF NOT EXISTS GameUsers
(
    gameId      VARCHAR(36),
    userId      VARCHAR(36),
    gameKey     VARCHAR(36),
    PRIMARY KEY (gameId, userId)
);
CREATE TABLE IF NOT EXISTS Dms
(
    id          VARCHAR(36),
    fromId      VARCHAR(36),
    toId        VARCHAR(36),
    created     BIGINT,
    message     VARCHAR(500),
    state       VARCHAR(10),
    PRIMARY KEY (id)
);
EOF