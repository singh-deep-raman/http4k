package org.example.web

import org.http4k.core.ContentType
import org.http4k.core.Method
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.Uri
import org.http4k.core.body.form
import org.http4k.lens.contentType
import org.http4k.routing.bind
import org.http4k.routing.routes

// get the content for this webpage https://developers.google.com/identity/gsi/web/guides/display-button
// you need to create or setup an app on your gcp console project so that you get a clientId
fun webApp(clientId: String, redirectUri: Uri) = routes(
    "/ui" bind Method.GET to {
        Response(Status.OK)
            .contentType(ContentType.TEXT_HTML)
            .body("""
                <html>
                  <body>
                    <script src="https://accounts.google.com/gsi/client" async></script>
                    <div id="g_id_onload"
                        data-client_id="$clientId"
                        data-login_uri="$redirectUri "
                        data-auto_prompt="false">
                    </div>
                    <div class="g_id_signin"
                        data-type="standard"
                        data-size="large"
                        data-theme="outline"
                        data-text="sign_in_with"
                        data-shape="rectangular"
                        data-logo_alignment="left">
                    </div>
                  <body>
                </html>
            """.trimIndent())
    },

    "redirect" bind Method.POST to { request ->
        val form = request.form("credential") ?: "error"
        Response(Status.OK).body(form)
    }
)