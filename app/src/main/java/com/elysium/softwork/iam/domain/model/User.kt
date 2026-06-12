package com.elysium.softwork.iam.domain.model

/**
 * IAM user entity — the single identity shape shared by every IAM flow.
 *
 * One immutable data class flows end-to-end through every IAM operation, so all fields
 * are nullable: different operations fill different subsets. For example, a login request
 * carries only [email] + [password], while the resolved session fills [id], [username],
 * [role], and [token].
 *
 * The class is framework-agnostic by design: property names match the backend wire keys
 * exactly, so the data layer's JSON serializer resolves them by reflection without any
 * mapping annotations. If the wire contract ever diverges from these names, introduce a
 * dedicated DTO in the data layer and map it into this entity — do not re-couple the
 * domain to serializer annotations.
 *
 * @property id server-issued user identifier (response only).
 * @property username display name shown across the Employee UI.
 * @property email corporate email used for sign-in.
 * @property password plain-text password sent on register/login. Never persisted client-side.
 * @property role business role string. The Employee client only emits `"EMPLOYEE"`.
 * @property token JWT issued by the server on a successful login or registration.
 */
data class User(
    val id: String? = null,
    val username: String? = null,
    val email: String? = null,
    val password: String? = null,
    val role: String? = null,
    val token: String? = null,
)
