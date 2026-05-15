package com.elysium.softwork.iam.domain.model

import com.google.gson.annotations.SerializedName

/**
 * Server-issued JWT envelope. Some endpoints respond with the token bundled inside [User]
 * (`User.token`); others may return a stand-alone payload — this class covers the latter shape.
 *
 * @property token raw JWT string ready to be placed in the `Authorization: Bearer …` header.
 * @property expiresAt epoch millis when the token loses validity. `0` if the server omits it.
 */
data class AuthToken(
    @SerializedName("token") val token: String,
    @SerializedName("expiresAt") val expiresAt: Long = 0L,
)
