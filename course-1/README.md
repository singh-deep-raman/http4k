# Learn http4k-core

* Http4k Core - https://www.http4k.org/ecosystem/http4k/
* SQLDelight - https://sqldelight.github.io/sqldelight/latest/jvm_mysql/
* Kotest - https://kotest.io/docs/framework/framework.html
* MariaDB Docker - https://hub.docker.com/_/mariadb
  * Make sure you create a user which will have access to the mariadb
  * By default root user has permissions on localhost only, but in docker container localhost points to the container itself
  * So you need to create a user to access the maria db like using below command:

CREATE USER 'appuser'@'%' IDENTIFIED BY 'apppass';
GRANT ALL PRIVILEGES ON database-name.* TO 'appuser'@'%';
FLUSH PRIVILEGES;


## Plugins
* SqlDelight IntelliJ plugin doesn't work with latest IntelliJ versions
* 