package org.example.api

import org.example.docs.catDataSample
import org.example.docs.catSample
import org.example.model.Cat
import org.example.model.CatDto
import org.example.service.CatService
import org.http4k.contract.contract
import org.http4k.contract.div
import org.http4k.contract.meta
import org.http4k.contract.openapi.ApiInfo
import org.http4k.contract.openapi.v3.OpenApi3
import org.http4k.contract.openapi.v3.OpenApi3ApiRenderer
import org.http4k.core.*
import org.http4k.format.Moshi
import org.http4k.format.Moshi.auto
import org.http4k.lens.Path
import org.http4k.lens.RequestKey
import org.http4k.lens.uuid
import org.http4k.routing.RoutingHttpHandler
import org.http4k.security.BearerAuthSecurity

// we should use the same lens in our tests as well, so that if someone changes the lens here, tests will break
val catIdLens = Path.uuid().of("catIdUsingLens")
val catLens = Body.auto<Cat>().toLens()
val catsLens = Body.auto<Array<Cat>>().toLens()
val catBodyLens = Body.auto<CatDto>().toLens()

// HttpHandler talks about one route, RoutingHttpHandler talks about multiple routes and which route is tied to which handler
fun CatService.api(): RoutingHttpHandler {
    val userIdLens = RequestKey.required<String>("userId")
    // we need this for all of our secure endpoints, it will allow swagger to pass token to hit the apis
    val jwtSecurity = BearerAuthSecurity(userIdLens, ::verify, "googleAuth")

    // contract helps you return the same RoutingHttpHandler with swagger documentation as well
    return contract {
        // although you have specified swagger docs for all the endpoints, you need to render that using a renderer
        // default openapi renderer is jackson, but we can use Moshi using following code
        descriptionPath = "openapi.json"
        renderer = OpenApi3(
            apiInfo = ApiInfo("Cats API", "v1"),
             apiRenderer = OpenApi3ApiRenderer(Moshi), // this is a fallback, but it has some limitations in contrast to Jackson, like showing object-124** instead of model name, which is not user/developer friendly
            json = Moshi
        )

        routes += "/v1/cats" meta {
            operationId = "v1ListCats"
            summary = "List Cats"

            returning(status = Status.OK, body = catsLens to arrayOf(catSample))
        } bindContract Method.GET to { _: Request ->
            val cats = listCats()
            Response(Status.OK).body(Moshi.asFormatString(cats))
        }

        // operator overloading done for div() method and using / instead
        routes += "/v1/cats" / catIdLens meta {
            operationId = "v1GetCat"
            summary = "Get Cat"

            returning(Status.OK, catLens to catSample)
            returning(Status.NOT_FOUND to "cat not found")
        } bindContract Method.GET to { catId ->
            {
                // val catId = catIdLens(request) // now you don't need to get the value using lens as using div() method you get the parameter directly
                val cat = getCat(catId)
                cat?.let { Response(Status.OK).body(Moshi.asFormatString(it)) } ?: Response(Status.NOT_FOUND)
            }
        }

        // now the secure endpoints (to create a cat)
        routes += "/v1/cats" meta {
            operationId = "v1CreateCat"
            summary = "Create Cat"
            security = jwtSecurity

            receiving(catBodyLens to catDataSample)

            returning(Status.OK, catLens to catSample)
            returning(Status.UNAUTHORIZED to "bearer token required")
        } bindContract Method.POST to { request: Request ->
            val catDto = catBodyLens(request)
            val userId = userIdLens(request)
            val addedCat = addCat(userId, catDto)
            Response(Status.CREATED).with(catLens of addedCat)
        }

        // secured endpoint to delete the cat
        routes += "/v1/cats" / catIdLens meta {
            operationId = "v1DeleteCat"
            summary = "Delete Cat"
            security = jwtSecurity

            returning(Status.OK, catLens to catSample)
            returning(Status.UNAUTHORIZED to "bearer token required")
            returning(Status.FORBIDDEN to "don't have permission to delete cat")
        } bindContract Method.DELETE to { catId ->
            fnname@{ request: Request ->
                val userId = userIdLens(request)
                val cat = getCat(catIdLens(request)) ?: return@fnname Response(Status.NOT_FOUND)

                if (cat.userId != userId) return@fnname Response(Status.FORBIDDEN)

                val deleteCat = deleteCat(catIdLens(request))
                deleteCat?.let {
                    Response(Status.NO_CONTENT)
                } ?: Response(Status.NOT_FOUND)
            }
        }
    }

    // this is the implementation of our api endpoints, have it here for reference to add it to the swagger docs
//    return routes(
//        // below is a controller endpoint, you can add as many as you want
//        "/v1/cats" bind Method.GET to {
//            val cats = listCats()
//            Response(Status.OK).body(Moshi.asFormatString(cats))
//        },
//
//        // get by id endpoint without lens (see problems !! is being used at multiple places)
//        "/v1/cats/{catId}" bind Method.GET to { request ->
//            // we are trying to get catId, but it can be null also
//            val pathParamCatId = request.path("catId")!!
//            val cat = getCat(UUID.fromString(pathParamCatId))
//            cat?.let { Response(Status.OK).body(Moshi.asFormatString(cat)) }
//                ?: Response(Status.NOT_FOUND)
//        },
//
//        // get by id endpoint with http4k lenses
//        // An http4k lens is a way to get something in and out of a request
//        "/v1/cats-with-lens/$catIdLens" bind Method.GET to { request ->
//            val catId = catIdLens(request)
//            val cat = getCat(catId)
//
//            cat?.let { Response(Status.OK).body(Moshi.asFormatString(it)) } ?: Response(Status.NOT_FOUND)
//        },
//
//        // we can use lens to get body out of a response instead of Moshi like below
//        "/v1/cats-with-body-lens/$catIdLens" bind Method.GET to { request ->
//            val catId = catIdLens(request)
//            val cat = getCat(catId)
//
//            cat?.let {
//                Response(Status.OK)
//                    .with(catLens of cat)
//            } ?: Response(Status.NOT_FOUND)
//        },
//
//        // we can use lens to get request body from the request
//        ServerFilters.BearerAuth(userIdLens, ::verify).then(routes(
//            "/v1/cats" bind Method.POST to { request ->
//                val catDto = catBodyLens(request)
//                val userId = userIdLens(request)
//                val addedCat = addCat(userId, catDto)
//                Response(Status.CREATED)
//                    .with(catLens of addedCat)
//            },
//
//            "/v1/cats/$catIdLens" bind Method.DELETE to fnname@ { request ->
//                val userId = userIdLens(request)
//                val cat = getCat(catIdLens(request)) ?: return@fnname Response(Status.NOT_FOUND)
//
//                if (cat.userId != userId)
//                    return@fnname Response(Status.FORBIDDEN)
//
//                val deleteCat = deleteCat(catIdLens(request))
//                deleteCat?.let {
//                    Response(Status.NO_CONTENT)
//                } ?: Response(Status.NOT_FOUND)
//            }
//        ))
//
//    )
}
