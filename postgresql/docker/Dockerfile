# Based on <https://hub.docker.com/_/postgres/>

FROM postgres:15.5

COPY ./configure-postgres.sh /docker-entrypoint-initdb.d/
COPY ./configure-koutainternal-database.sh /docker-entrypoint-initdb.d/