package com.elysium.softwork.shared.data.network

/**
 * Client-side mirror of the backend's `GlobalExceptionHandler` 400 validation payload.
 *
 * The Spring Boot API returns this shape for `MethodArgumentNotValidException` and internal
 * business-validation failures:
 *
 * ```json
 * {
 *   "status": 400,
 *   "error": "Bad Request",
 *   "message": "Internal validation failed",
 *   "field_errors": { "argument": "[CreateUserCommand] dni must be 8 characters long" }
 * }
 * ```
 *
 * Framework-agnostic by design: property names match the wire keys exactly so Gson resolves
 * them by reflection without `@SerializedName`. In particular [field_errors] keeps the
 * snake_case wire name as its Kotlin identifier so no annotation is needed.
 *
 * @property status numeric HTTP status echoed in the body (always `400` for this shape).
 * @property error short reason phrase (`"Bad Request"`).
 * @property message human-readable summary of the rejection.
 * @property field_errors map of offending field → validation message. Nullable because some
 *   400s (internal `IllegalArgumentException`) carry only [message] with no per-field detail.
 */
data class BadRequestResponse(
    val status: Int = 400,
    val error: String? = null,
    val message: String? = null,
    val field_errors: Map<String, String>? = null,
) {

    /**
     * Best-effort single user-facing message extracted from this payload.
     *
     * Prefers the conventional `"argument"` key the backend uses for command-level
     * validation (e.g. the DNI length rule), then falls back to the first field error, then
     * to the top-level [message]. Returns `null` only when the payload is entirely empty.
     */
    fun primaryFieldError(): String? =
        field_errors?.get(ARGUMENT_KEY)
            ?: field_errors?.values?.firstOrNull()
            ?: message

    companion object {
        /** Conventional `field_errors` key the backend uses for command-argument rules. */
        const val ARGUMENT_KEY: String = "argument"
    }
}

/**
 * Typed failure raised by the data layer when the backend answers `400 Bad Request`.
 *
 * Carries the already-deserialized [response] so the presentation layer can pull the
 * offending-field message straight onto the form state without re-touching the raw HTTP
 * error stream. Surfaced through `Result.failure` by the IAM store.
 *
 * @property response the parsed validation payload (never null; an unparseable body yields a
 *   [BadRequestResponse] with only [BadRequestResponse.message] populated).
 */
class BadRequestException(
    val response: BadRequestResponse,
) : RuntimeException(response.primaryFieldError() ?: "Bad Request")
