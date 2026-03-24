package org.example.api

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.kotest.matchers.be
import org.example.*
import org.example.config.audience
import org.example.config.dbUrl
import org.example.config.issuer
import org.example.config.publicKey
import org.example.model.Cat
import org.example.model.CatDto
import org.http4k.base64Encode
import org.http4k.config.Environment
import org.http4k.core.*
import org.http4k.format.Moshi.auto
import org.http4k.kotest.shouldHaveBody
import org.http4k.kotest.shouldHaveStatus
import org.http4k.lens.bearerAuth
import org.http4k.testing.Approver
import org.http4k.testing.JsonApprovalTest
import org.http4k.testing.assertApproved
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.security.KeyPairGenerator
import java.security.interfaces.RSAPrivateKey
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.*


// Use Testing: Approval to compare the responses of the api, we can't rely on lens because the data might be in bytes,
// but when we use the lens to create a model/pojo, we won't know, so we need to do the JSON comparison here
// Link - https://www.http4k.org/ecosystem/http4k/reference/approvaltests/
@ExtendWith(JsonApprovalTest::class)
class ApiKotestTest {

    private val keyPair = KeyPairGenerator.getInstance("RSA")
        .apply { this.initialize(2048) }
        .generateKeyPair()

    /**
     * 🔹 DB_CLOSE_DELAY=-1
     * This is important.
     * Normally:
     * H2 in-memory DB is destroyed when the last connection closes.
     * With:
     * DB_CLOSE_DELAY=-1
     * It means:
     * Keep the database alive until the JVM shuts down.
     * This is extremely useful in tests because:
     * Multiple connections can reuse the same in-memory DB
     * The DB won’t disappear between transactions
     */

    private val catService = createApp(
        Environment.defaults(
            dbUrl of "jdbc:h2:mem:${UUID.randomUUID()};DB_CLOSE_DELAY=-1",
            publicKey of keyPair.public.encoded.base64Encode(),
            issuer of "cats_idp",
            audience of "cats_app"
        ),
        Clock.fixed(Instant.parse("2026-02-14T12:13:14Z"), ZoneId.of("UTC")),
        { UUID.fromString("11111111-1111-1111-1111-111111111111") }
    )

    private val catApi = catService.api()
    private val catLens = Body.auto<Cat>().toLens()

    @Test
    fun `should return get cat by id`() {
        val expectedCat = catService.addCat(
            "user1",
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
    fun `should create a cat using lens for payload and also JsonApprovalTest for testing JSON`(approver: Approver) {
        val response = Request(Method.POST, "/v1/cats")
            .with(
                catBodyLens of CatDto(
                    "Test Louis", LocalDate.parse("2026-01-01"), "test-breed", "test-color"
                )
            )
            .bearerAuth(createToken("userId1"))
            .let(catApi)

        // it is going to create a JSON file with test-class name and test name.ACTUAL in test/resources folder
        // you need to create a .APPROVED file which will have the expected response with same name as above
        approver.assertApproved(response, Status.CREATED)
    }

    @Test
    fun `should delete a cat`() {
        val expectedCat = catService.addCat(
            "user3",
            CatDto(
                "Louis", LocalDate.now(), "don't know", "brown"
            )
        )
        Request(Method.DELETE, "/v1/cats/${expectedCat.id}")
            .bearerAuth(createToken("user3"))
            .let(catApi)
            .shouldHaveStatus(Status.NO_CONTENT)
    }

    @Test
    fun `should return Unauthorised for api without token`() {
        val cat = catService.addCat(
            "user3",
            CatDto(
                "Louis", LocalDate.now(), "don't know", "brown"
            )
        )

        Request(Method.DELETE, "/v1/cats/${cat.id}")
            .bearerAuth(createToken("user2 "))
            .let(catApi)
            .shouldHaveStatus(Status.FORBIDDEN)

    }

    @Test
    fun `should return Forbidden for api for deleting a cat of another user`() {
        Request(Method.DELETE, "/v1/cats/${UUID.randomUUID()}")
            .let(catApi)
            .shouldHaveStatus(Status.UNAUTHORIZED)

    }

    @Test
    fun `should generate openapi documentation`(approver: Approver) {
         Request(Method.GET, "openapi.json")
             .let(catApi)
             .also { approver.assertApproved(it, Status.OK) }
     }

    fun createToken(userId: String): String {
        val algorithm: Algorithm = Algorithm.RSA256(null, keyPair.private as RSAPrivateKey)
        return JWT.create()
            .withSubject(userId)
            .withIssuer("cats_idp")
            .withAudience("cats_app")
            .sign(algorithm)
    }
}