# Learn http4k-core

* Http4k Core - https://www.http4k.org/ecosystem/http4k/
* SQLDelight - https://sqldelight.github.io/sqldelight/latest/jvm_mysql/
* Kotest - https://kotest.io/docs/framework/framework.html
* MariaDB Docker - https://hub.docker.com/_/mariadb
  * Make sure you create a user which will have access to the mariadb
  * By default root user has permissions on localhost only, but in docker container localhost points to the container itself
  * So you need to create a user to access the maria db like using below command:
* There are many JWKS libraries. Common JWKS libraries include 'Nimbus JOSE JWT (very popular)', 'jwks-rsa', 'auth0 java-jwt'
  * We are going to use JWKS library - https://github.com/auth0/jwks-rsa-java
  * Spring Security ues Nimbus library in the background
* Google docs for OAuth 2.0 - https://developers.google.com/identity/protocols/oauth2?authuser=3 also https://developers.google.com/identity/gsi/web/guides/verify-google-id-token
  * You need to configure the Google Auth Platform first before creating any clients there
  * 

CREATE USER 'appuser'@'%' IDENTIFIED BY 'apppass';
GRANT ALL PRIVILEGES ON database-name.* TO 'appuser'@'%';
FLUSH PRIVILEGES;


## Plugins
* SqlDelight IntelliJ plugin doesn't work with latest IntelliJ versions
* 