FROM php:8.2-apache

VOLUME /tmp

EXPOSE 80

ADD ./index.php /var/www/html/index.php

ENTRYPOINT ["apache2-foreground"]
