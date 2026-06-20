package com.elysium.softwork.iam.domain.model

/**
 * IAM user/profile entity — the single annotation-free bean that flows through every IAM
 * endpoint (the *Bean / Pragmatic Shortcut*).
 *
 * One immutable data class carries the request **and** the response of `sign-in`,
 * `sign-up/employee`, and `employee-profile`. Because those contracts overlap only
 * partially — and the backend deliberately uses different wire keys for the *same* concept
 * on request vs. response — every field is nullable and the asymmetric keys coexist side by
 * side. Gson resolves each by reflection from the property name, so **no `@SerializedName`
 * is present or permitted here**. The cost is that one class describes several wire shapes;
 * the win is zero DTO/mapper boilerplate.
 *
 * Documented wire asymmetries this bean absorbs (see API_DOCUMENTATION.md):
 * - sign-in **request** sends [email]; the sign-in **response** returns the same address
 *   under [gmail].
 * - employee-profile **request** sends [start_date]; the profile **response** returns the
 *   same date under [dateStart].
 *
 * @property id user-account id returned by `sign-in` / `sign-up` (response key `id`).
 * @property gmail email echoed by the sign-in **response** (wire key `gmail`).
 * @property email email sent on the sign-in / sign-up **request** (wire key `email`).
 * @property password plain-text password sent on login/registration. Never returned populated.
 * @property token JWT issued on a successful `sign-in` / `sign-up`.
 * @property membershipStatus subscription tier reported alongside the session (`ACTIVE`,
 *   `PENDING`, `INACTIVE`). Drives the membership gate; `null` when the backend omits it.
 * @property name worker first name — employee sign-up request field.
 * @property lastName worker last name — employee sign-up request field.
 * @property phoneNumber contact phone — employee sign-up request field.
 * @property dni national id — employee sign-up request field (8 chars; see the 400 rule).
 * @property anonymousName forum/survey pseudonym — employee sign-up request field.
 * @property position job title — employee sign-up / profile field.
 * @property salary monthly salary — employee sign-up / profile field.
 * @property start_date profile start date on the **creation request** (wire key `start_date`).
 * @property dateStart profile start date on the **profile response** (wire key `dateStart`).
 * @property employee_profile_id profile primary key returned by `employee-profile` responses.
 * @property user_account_id owning account id on `employee-profile` responses; matched against
 *   the persisted account id to find this worker's profile row in the list endpoint.
 * @property work_of_team_id team association on `employee-profile` responses.
 */
data class User(
    // --- Authentication (sign-in / sign-up) ---
    val id: Long? = null,
    val gmail: String? = null,
    val email: String? = null,
    val password: String? = null,
    val token: String? = null,
    val membershipStatus: String? = null,

    // --- Employee sign-up request payload ---
    val name: String? = null,
    val lastName: String? = null,
    val phoneNumber: String? = null,
    val dni: String? = null,
    val anonymousName: String? = null,
    val position: String? = null,
    val salary: Int? = null,
    val start_date: String? = null,

    // --- Employee profile response payload ---
    val dateStart: String? = null,
    val employee_profile_id: Long? = null,
    val user_account_id: Long? = null,
    val work_of_team_id: Long? = null,
) {

    /**
     * `true` only when the backend explicitly reports an `ACTIVE` membership. A `null`
     * status (the backend currently omits the field on sign-in) is treated as **not active**,
     * so the worker is routed through the payment onboarding gate until a successful
     * subscription is recorded.
     */
    fun isMembershipActive(): Boolean = membershipStatus.equals(MEMBERSHIP_ACTIVE, ignoreCase = true)

    companion object {
        /** Wire value of the active membership tier. */
        const val MEMBERSHIP_ACTIVE: String = "ACTIVE"
    }
}
