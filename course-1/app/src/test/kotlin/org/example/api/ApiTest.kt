package org.example.api

import org.example.model.Cat
import org.example.model.CatDto
import org.example.service.CatService
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Status
import org.http4k.format.Moshi
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

// it is by far the easiest to test http4k HttpHandler because it is just a function
// we need not to start a server on the localhost to test the api, so everything is in the memory
// that is what makes http4k testing so easy and it will be like a unit test only
class ApiTest {

    private val fixedClock: Clock = Clock.fixed(Instant.parse("2026-02-04T00:00:00Z"), ZoneOffset.UTC)
    private val catService = CatService(fixedClock)

    private val api = catService.api()

    @Test
    fun `list cats with empty response`() {
        val request = Request(Method.GET, "/v1/cats")

        val response = api(request)

        assertEquals(Status.OK, response.status)

    }

    @Test
    fun `list cats with single cat`() {
        val expectedCat1 = catService.addCat(
            CatDto(
                "Louis", LocalDate.now(), "don't know", "brown"
            )
        )
        val expectedCat2 = catService.addCat(
            CatDto(
                "Mike", LocalDate.now(), "know", "white"
            )
        )
        val request = Request(Method.GET, "/v1/cats")

        val response = api(request)

        assertEquals(Status.OK, response.status)
        assertEquals(
            listOf(expectedCat1, expectedCat2),
            // response.body // it returns the json object
            Moshi.asA<Array<Cat>>(response.bodyString()).toList()
        )

    }

}