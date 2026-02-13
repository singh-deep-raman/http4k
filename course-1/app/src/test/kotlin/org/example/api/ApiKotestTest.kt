package org.example.api

import io.kotest.matchers.be
import org.example.model.Cat
import org.example.model.CatDto
import org.example.service.CatService
import org.http4k.core.Body
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Status
import org.http4k.format.Moshi.auto
import org.http4k.kotest.shouldHaveBody
import org.http4k.kotest.shouldHaveStatus
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.*

class ApiKotestTest {

    private val catService = CatService(
        Clock.fixed(Instant.parse("2026-02-14T12:13:14Z"), ZoneId.of("UTC"))
    )

    private val catApi = catService.api()
    private val catLens = Body.auto<Cat>().toLens()

    @Test
    fun `should return get cat by id`() {
        val expectedCat = catService.addCat(
            CatDto(
                "Louis", LocalDate.now(), "don't know", "brown"
            )
        )

        val request = Request(Method.GET, "/v1/cats/${expectedCat.id}")

        val response = catApi(request)

        // kotest assertions
        response shouldHaveStatus Status.OK
    }

    @Test
    fun `more compact kotest for not found cat by id`() {
        Request(Method.GET, "/v1/cats/${UUID.randomUUID()}")
            .let(catApi)
            .shouldHaveStatus(Status.NOT_FOUND)

    }

    @Test
    fun `kotest assert status code and body `() {
        val expectedCat = catService.addCat(
            CatDto(
                "Louis", LocalDate.now(), "don't know", "brown"
            )
        )

        val response = Request(Method.GET, "/v1/cats-with-lens/${expectedCat.id}")
            .let(catApi)

        response.shouldHaveStatus(Status.OK)
        response.shouldHaveBody(catLens, be(expectedCat))

    }

}