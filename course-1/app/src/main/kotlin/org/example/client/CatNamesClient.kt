package org.example.client

import org.example.client.namesListLens
import org.http4k.core.Body
import org.http4k.core.HttpHandler
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Uri
import org.http4k.core.with
import org.http4k.format.Moshi.auto
import org.http4k.lens.Query
import org.http4k.lens.int

val limitLens = Query.int().optional("limit")
val namesListLens = Body.auto<Array<String>>().toLens() // we can't use List<> here because of some problem with Moshi, I think it will work fine with Jackson

class CatNamesClient(
    private val host: Uri,
    private val internet: HttpHandler // we are injecting internet handler here so that we can inject a fake handler for our tests
) {

    fun getCatNames(limit: Int? = 1): List<String> {
        val request = Request(Method.GET, host.path("/api/cats/v1"))
            .with(limitLens of limit)

        val response = internet(request)

        if (!response.status.successful) error(response)

        return namesListLens(response).toList()
    }
}