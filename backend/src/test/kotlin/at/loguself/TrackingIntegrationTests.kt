package at.loguself

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.jdbc.core.JdbcTemplate
import tools.jackson.databind.ObjectMapper
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = [
    "spring.flyway.default-schema=loguself_test",
    "spring.datasource.hikari.connection-init-sql=SET search_path TO loguself_test"
])
class TrackingIntegrationTests {
    @LocalServerPort private var port: Int = 0
    @Autowired private lateinit var jdbc: JdbcTemplate
    @Autowired private lateinit var mapper: ObjectMapper
    private val client = HttpClient.newHttpClient()
    private fun request(path: String, body: String? = null): HttpResponse<String> {
        val builder = HttpRequest.newBuilder(URI("http://127.0.0.1:$port/api$path"))
        if (body == null) builder.GET() else builder.header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(body))
        return client.send(builder.build(), HttpResponse.BodyHandlers.ofString())
    }

    @Test
    fun `persist records under correct ticket and reject invalid input without changing totals`() {
        val created = request("/tickets", """{"projectId":"00000000-0000-0000-0000-000000000001","entryTypeId":"00000000-0000-0000-0000-000000000002","title":"Integrationstest"}""")
        assertEquals(201, created.statusCode(), created.body())
        val id = mapper.readTree(created.body())["id"].asText()
        try {
            for (minutes in listOf(120, 30)) {
                val response = request("/tickets/$id/records", """{"date":"2026-10-22","durationMinutes":$minutes,"sport":"Laufen"}""")
                assertEquals(201, response.statusCode(), response.body())
            }
            for (minutes in listOf(0, -5)) {
                assertEquals(400, request("/tickets/$id/records", """{"date":"2026-10-22","durationMinutes":$minutes,"sport":"Laufen"}""").statusCode())
            }
            assertEquals(400, request("/tickets/$id/records", """{"date":"2026-10-22","durationMinutes":20,"sport":" "}""").statusCode())
            val result = mapper.readTree(request("/tickets/$id/records").body())
            assertEquals(150, result["totalMinutes"].asInt())
            assertEquals(2, result["records"].size())
            assertEquals("2026-10-22", result["records"][0]["date"].asText())
            assertEquals("Laufen", result["records"][0]["fields"]["sport"].asText())
            assertEquals(2L, jdbc.queryForObject("SELECT count(*) FROM records WHERE ticket_id = CAST(? AS uuid)", Long::class.java, id))
            assertEquals(404, request("/tickets/10000000-0000-0000-0000-000000000000/records").statusCode())
        } finally {
            jdbc.update("DELETE FROM records WHERE ticket_id = CAST(? AS uuid)", id)
            jdbc.update("DELETE FROM tickets WHERE id = CAST(? AS uuid)", id)
        }
    }
}
