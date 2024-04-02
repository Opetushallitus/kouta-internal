# Based on <https://hub.docker.com/_/postgres/>

FROM postgres:12.17

COPY ./configure-postgres.sh /docker-entrypoint-initdb.d/
COPY ./configure-koutainternal-database.sh /docker-entrypoint-initdb.d/