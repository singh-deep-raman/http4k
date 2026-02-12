package org.example.api

import org.example.service.CatService
import org.http4k.core.HttpHandler
import org.http4k.core.Method
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.format.Moshi
import org.http4k.routing.bind
import org.http4k.routing.routes

fun CatService.api(): HttpHandler {
    return routes(
        // below is a controller endpoint, you can add as many as you want
        "/v1/cats" bind Method.GET to {
            val cats = listCats()
             Response(Status.OK).body(Moshi.asFormatString(cats))
        }
    )
}