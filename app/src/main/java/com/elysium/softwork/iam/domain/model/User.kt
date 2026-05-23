package com.elysium.softwork.iam.domain.model

import com.google.gson.annotations.SerializedName

/**
 * IAM user — used as both **request body** and **response payload** in the Retrofit
 * [com.elysium.softwork.iam.data.network.AuthWebService].
 *
 * One data class flows end-to-end through every IAM endpoint, so all fields are nullable:
 * different endpoints fill different subsets. For example, login requests carry only
 * [email] + [password], while the server response fills [id], [username], [role], and
 * [token]. The two-way reuse avoids a DTO/assembler layer for the small IAM surface.
 *
 * @property id server-issued user identifier (response only).
 * @property username display name shown across the Employee UI.
 * @property email institutional/corporate email used for sign-in.
 * @property password plain-text password sent on register/login. Never persisted client-side.
 * @property role business role string. The Employee client only emits `"EMPLOYEE"`.
 * @property token JWT issued by the server on a successful login or registration.
 */
data class User(
    @SerializedName("id") val id: String? = null,
    @SerializedName("username") val username: String? = null,
    @SerializedName("email") val email: String? = null,
    @SerializedName("password") val password: String? = null,
    @SerializedName("role") val role: String? = null,
    @SerializedName("token") val token: String? = null,
)
