package org.example.api

import org.example.model.Cat
import org.example.model.CatDto
import org.example.service.CatService
import org.http4k.core.Body
import org.http4k.core.HttpHandler
import org.http4k.core.Method
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.with
import org.http4k.format.Moshi
import org.http4k.format.Moshi.auto
import org.http4k.lens.Path
import org.http4k.lens.uuid
import org.http4k.routing.bind
import org.http4k.routing.path
import org.http4k.routing.routes
import java.util.UUID

// we should use the same lens in our tests as well, so that if someone changes the lens here, tests will break
val catIdLens = Path.uuid().of("catIdUsingLens")
val catLens = Body.auto<Cat>().toLens()
val catBodyLens = Body.auto<CatDto>().toLens()

fun CatService.api(): HttpHandler {
    return routes(
        // below is a controller endpoint, you can add as many as you want
        "/v1/cats" bind Method.GET to {
            val cats = listCats()
            Response(Status.OK).body(Moshi.asFormatString(cats))
        },

        // get by id endpoint without lens (see problems !! is being used at multiple places)
        "/v1/cats/{catId}" bind Method.GET to { request ->
            // we are trying to get catId, but it can be null also
            val pathParamCatId = request.path("catId")!!
            val cat = getCat(UUID.fromString(pathParamCatId))
            cat?.let { Response(Status.OK).body(Moshi.asFormatString(cat)) }
                ?: Response(Status.NOT_FOUND)
        },

        // get by id endpoint with http4k lenses
        // An http4k lens is a way to get something in and out of a request
        "/v1/cats-with-lens/$catIdLens" bind Method.GET to { request ->
            val catId = catIdLens(request)
            val cat = getCat(catId)

            cat?.let { Response(Status.OK).body(Moshi.asFormatString(it)) } ?: Response(Status.NOT_FOUND)
        },

        // we can use lens to get body out of a response instead of Moshi like below
        "/v1/cats-with-body-lens/$catIdLens" bind Method.GET to { request ->
            val catId = catIdLens(request)
            val cat = getCat(catId)

            cat?.let {
                Response(Status.OK)
                    .with(catLens of cat)
            } ?: Response(Status.NOT_FOUND)
        },

        // we can use lens to get request body from the request
        "/v1/cats" bind Method.POST to { request ->
            val catDto = catBodyLens(request)
            val addedCat = addCat(catDto)
            Response(Status.CREATED)
                .with(catLens of addedCat)
        },

        "/v1/cats/$catIdLens" bind Method.DELETE to { request ->
            val deleteCat = deleteCat(catIdLens(request))
            deleteCat?.let {
                Response(Status.NO_CONTENT)
            } ?: Response(Status.NOT_FOUND)
        }


    )
}
