#!/bin/bash

set -e

source .env

if [ "$MYSQL_HOST" == "localhost" ]; then
    MYSQL_HOST="127.0.0.1"
fi

mysql -u "${MYSQL_USER}" -h "${MYSQL_HOST}" "-p${MYSQL_PASS}" << EOF
USE ${MYSQL_DB};

DROP TABLE IF EXISTS Users;
DROP TABLE IF EXISTS UserKeys;
DROP TABLE IF EXISTS UserFriends;
DROP TABLE IF EXISTS UserAuthDetails;
DROP TABLE IF EXISTS Games;
DROP TABLE IF EXISTS GameUsers;
EOF

. $(dirname $0)/create-databases.sh