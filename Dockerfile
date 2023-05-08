FROM BASEIMAGE

ARG NAME
ARG ARTIFACT_DESTINATION
ENV NAME=${NAME:-kouta-internal}
ENV ARTIFACT_DESTINATION=${ARTIFACT_DESTINATION:-/usr/local/bin}

COPY config /etc/oph/
COPY artifact $ARTIFACT_DESTINATION/

RUN ln -sf /usr/share/zoneinfo/Europe/Helsinki /etc/localtime

EXPOSE 8080

# Ensure no setuid / setgid binaries exist
RUN find / -xdev -perm +6000 -type f -not -name sudo -exec chmod a-s {} \; || true

WORKDIR /home/oph
USER oph

CMD ["sh", "/usr/local/bin/run"]