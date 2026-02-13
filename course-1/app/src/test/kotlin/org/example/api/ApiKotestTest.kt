package org.example.api

import io.kotest.matchers.be
import org.example.model.Cat
import org.example.model.CatDto
import org.example.service.CatService
import org.http4k.core.*
import org.http4k.format.Moshi.auto
import org.http4k.kotest.shouldHaveBody
import org.http4k.kotest.shouldHaveStatus
import org.http4k.testing.Approver
import org.http4k.testing.JsonApprovalTest
import org.http4k.testing.assertApproved
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.*
import kotlin.math.exp

// Use Testing: Approval to compare the responses of the api, we can't rely on lens because the data might be in bytes,
// but when we use the lens to create a model/pojo, we won't know, so we need to do the JSON comparison here
// Link - https://www.http4k.org/ecosystem/http4k/reference/approvaltests/
@ExtendWith(JsonApprovalTest::class)
class ApiKotestTest {

    private val catService = CatService(
        Clock.fixed(Instant.parse("2026-02-14T12:13:14Z"), ZoneId.of("UTC")),
        { UUID.fromString("11111111-1111-1111-1111-111111111111") }
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

    @Test
    fun `should create a cat using lens for payload and also JsonApprovalTest for testing JSON`(approver: Approver) {
        val response = Request(Method.POST, "/v1/cats")
            .with(catBodyLens of CatDto(
                "Test Louis", LocalDate.now(), "test-breed", "test-color"
            ))
            .let(catApi)

        // it is going to create a JSON file with test-class name and test name.ACTUAL in test/resources folder
        // you need to create a .APPROVED file which will have the expected response with same name as above
        approver.assertApproved(response, Status.CREATED)
    }

    @Test
    fun `should delete a cat`() {
        val expectedCat = catService.addCat(
            CatDto(
                "Louis", LocalDate.now(), "don't know", "brown"
            )
        )
        Request(Method.DELETE, "/v1/cats/${expectedCat.id}")
            .let(catApi)
            .shouldHaveStatus(Status.NO_CONTENT)
    }
}