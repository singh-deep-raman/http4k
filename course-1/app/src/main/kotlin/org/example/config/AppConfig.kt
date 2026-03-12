package org.example.config

import org.http4k.config.EnvironmentKey
import org.http4k.lens.base64
import org.http4k.lens.secret
import org.http4k.lens.string
import org.http4k.lens.uri

// DB environment variables
val dbUrl = EnvironmentKey.string().required("JDBC_DATABASE_URL")
val dbUser = EnvironmentKey.string().required("JDBC_DATABASE_USERNAME")
val dbPassword = EnvironmentKey.secret().required("JDBC_DATABASE_PASSWORD")

// JWT related environment variables
val publicKey = EnvironmentKey.base64().required("PUBLIC_KEY")
val issuer = EnvironmentKey.string().required("ISSUER")
val audience = EnvironmentKey.string().required("AUDIENCE")
val redirectUri = EnvironmentKey.uri().required("REDIRECT_URI")