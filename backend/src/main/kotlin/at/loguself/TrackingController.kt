package at.loguself

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive
import org.springframework.http.HttpStatus
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.*
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.server.ResponseStatusException
import tools.jackson.databind.ObjectMapper
import java.time.LocalDate
import java.util.UUID

data class TicketRequest(val projectId: UUID, val entryTypeId: UUID,
    @field:NotBlank val title: String, val description: String = "")
data class SportRequest(val date: LocalDate, @field:Positive val durationMinutes: Int,
    @field:NotBlank val sport: String, val description: String = "")

@RestController
@RequestMapping("/api")
class TrackingController(private val jdbc: JdbcTemplate, private val mapper: ObjectMapper) {
    @GetMapping("/projects")
    fun projects(): List<Map<String, Any?>> = jdbc.queryForList("SELECT id, name FROM projects ORDER BY name")

    @GetMapping("/entry-types")
    fun types(): List<Map<String, Any?>> = jdbc.queryForList("SELECT id, name, definition_version FROM entry_types ORDER BY name")

    @GetMapping("/tickets")
    fun tickets(): List<Map<String, Any?>> = jdbc.queryForList("SELECT * FROM tickets ORDER BY created_at, id")

    @GetMapping("/tickets/{id}")
    fun ticket(@PathVariable id: UUID): Map<String, Any?> =
        jdbc.queryForList("SELECT * FROM tickets WHERE id = ?", id).firstOrNull()
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket nicht gefunden.")

    @PostMapping("/tickets")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    fun createTicket(@Valid @RequestBody request: TicketRequest): Map<String, Any?> {
        if (jdbc.queryForObject("SELECT count(*) FROM projects WHERE id = ?", Long::class.java, request.projectId) != 1L)
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Projekt nicht gefunden.")
        // M1 exposes only the seeded Sport definition; arbitrary type editing follows later.
        if (request.entryTypeId != SPORT_TYPE || jdbc.queryForObject("SELECT count(*) FROM entry_types WHERE id = ?", Long::class.java, request.entryTypeId) != 1L)
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "M1 unterstützt nur den vorbereiteten Sport-Typ.")
        val id = UUID.randomUUID()
        jdbc.update("INSERT INTO tickets (id, project_id, entry_type_id, title, description) VALUES (?, ?, ?, ?, ?)",
            id, request.projectId, request.entryTypeId, request.title.trim(), request.description)
        return ticket(id)
    }

    @GetMapping("/tickets/{id}/records")
    fun records(@PathVariable id: UUID): Map<String, Any> {
        ticket(id)
        val rows = jdbc.queryForList("SELECT id, recorded_on::text AS date, duration_minutes, description, fields::text AS fields FROM records WHERE ticket_id = ? ORDER BY recorded_on, created_at, id", id)
            .map { row -> row.toMutableMap().also { it["fields"] = mapper.readTree(row["fields"] as String) } }
        val total = jdbc.queryForObject("SELECT COALESCE(sum(duration_minutes), 0) FROM records WHERE ticket_id = ?", Long::class.java, id)!!
        return mapOf("records" to rows, "totalMinutes" to total)
    }

    @PostMapping("/tickets/{id}/records")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    fun createRecord(@PathVariable id: UUID, @Valid @RequestBody request: SportRequest): Map<String, Any> {
        val ticket = ticket(id)
        if (ticket["entry_type_id"] != SPORT_TYPE)
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Das Ticket ist kein Sport-Ticket.")
        val recordId = UUID.randomUUID()
        val version = jdbc.queryForObject("SELECT definition_version FROM entry_types WHERE id = ?", Int::class.java, SPORT_TYPE)!!
        jdbc.update("INSERT INTO records (id, ticket_id, definition_version, recorded_on, duration_minutes, description, fields) VALUES (?, ?, ?, ?, ?, ?, CAST(? AS jsonb))",
            recordId, id, version, request.date, request.durationMinutes, request.description,
            mapper.writeValueAsString(mapOf("sport" to request.sport.trim())))
        return mapOf("id" to recordId)
    }

    companion object { val SPORT_TYPE: UUID = UUID.fromString("00000000-0000-0000-0000-000000000002") }
}

@RestControllerAdvice
class ApiErrors {
    @ExceptionHandler(MethodArgumentNotValidException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun invalid(ex: MethodArgumentNotValidException) = mapOf("message" to "Bitte Pflichtfelder ausfüllen und eine positive Dauer in Minuten angeben.",
        "fields" to ex.bindingResult.fieldErrors.map { it.field }.distinct())

    @ExceptionHandler(HttpMessageNotReadableException::class, MethodArgumentTypeMismatchException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun malformed() = mapOf("message" to "Ungültige Eingabe. Bitte Datum, IDs und Zahlen prüfen.")

    @ExceptionHandler(ResponseStatusException::class)
    fun status(ex: ResponseStatusException): org.springframework.http.ResponseEntity<Map<String, String>> =
        org.springframework.http.ResponseEntity.status(ex.statusCode).body(mapOf("message" to (ex.reason ?: "Anfrage fehlgeschlagen.")))
}
