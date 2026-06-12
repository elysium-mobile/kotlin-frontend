package com.elysium.softwork.iam.domain.model

/**
 * Server-issued JWT envelope. Some endpoints respond with the token bundled inside [User]
 * (`User.token`); others may return a stand-alone payload — this class covers the latter shape.
 *
 * Framework-agnostic by design: property names match the backend wire keys exactly so the
 * data layer's JSON serializer resolves them by reflection without mapping annotations.
 *
 * @property token raw JWT string ready to be placed in the `Authorization: Bearer …` header.
 * @property expiresAt epoch millis when the token loses validity. `0` if the server omits it.
 */
data class AuthToken(
    val token: String,
    val expiresAt: Long = 0L,
)
