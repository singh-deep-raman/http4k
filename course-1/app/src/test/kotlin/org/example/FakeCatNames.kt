package org.example

import org.example.client.limitLens
import org.example.client.namesListLens
import org.http4k.core.Method
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.with
import org.http4k.routing.bind
import org.http4k.routing.routes
import kotlin.random.Random

private val catNames = listOf("name1", "name2", "name3")

fun fakeCatNames(random: Random) = routes(
    "/api/cats/v1" bind Method.GET to {
        val limit = limitLens(it) ?: 1

        val response = buildList {
            repeat(limit) {
                add(catNames.random(random)) // if we don't inject random here, our tests won't be deterministic
            }
        }

        Response(Status.OK)
            .with(namesListLens of response.toTypedArray())
    }
)