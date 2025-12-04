FROM postgres:16

WORKDIR /ha

COPY ha_arbiter.sh /ha/ha_arbiter.sh
RUN chmod +x /ha/ha_arbiter.sh

CMD ["/bin/bash", "/ha/ha_arbiter.sh"]
