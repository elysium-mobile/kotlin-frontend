package com.elysium.softwork.iam.domain.model

import com.google.gson.annotations.SerializedName

/**
 * IAM user — used as both **request body** and **response payload** in the Retrofit
 * [com.elysium.softwork.iam.data.network.AuthWebService].
 *
 * The team chose a pragmatic shortcut for Phase 2: skip DTO/assembler boilerplate and let a
 * single data class flow end-to-end. All fields are nullable because different endpoints fill
 * different subsets — for example, login requests carry only [email] + [password], while the
 * server response fills [id], [username], [role], and [token].
 *
 * @property id server-issued user identifier (response only).
 * @property username display name shown across the Employee UI.
 * @property email institutional/corporate email used for sign-in.
 * @property password plain-text password sent on register/login. Never persisted client-side.
 * @property role business role string. Phase 2 only emits `"EMPLOYEE"`.
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
