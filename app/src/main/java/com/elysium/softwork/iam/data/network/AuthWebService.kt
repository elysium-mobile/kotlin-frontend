package com.elysium.softwork.iam.data.network

import com.elysium.softwork.iam.domain.model.User
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

/**
 * Retrofit contract for the live IAM endpoints of the FlowWork Spring Boot API.
 *
 * The same [User] bean carries both request bodies and response payloads (the bean
 * shortcut) — different endpoints fill different subsets of its nullable fields. Only the
 * employee paths are declared here: the HR/RRHH sign-up endpoint is intentionally absent,
 * since this client is exclusively the employee experience.
 *
 * All paths are **relative** — the host + `/` base lives in `BuildConfig.BACKEND_BASE_URL`
 * (resolved by `ApiClient`). The `Authorization` header is attached automatically by
 * `AuthInterceptor`; the two public auth paths below are skipped by it.
 */
interface AuthWebService {

    /**
     * Authenticates an existing worker. Send [User.email] + [User.password]; the response
     * fills [User.id] (the user-account id), [User.gmail], and [User.token].
     */
    @POST("api/v1/authentication/sign-in")
    suspend fun signIn(@Body credentials: User): Response<User>

    /**
     * Registers a new employee account. Send the employee sign-up subset of [User]
     * ([User.name], [User.lastName], [User.email], [User.password], [User.dni],
     * [User.anonymousName], [User.dateStart], [User.position], [User.salary]); the response
     * fills [User.id], [User.gmail], and [User.token].
     */
    @POST("api/v1/authentication/sign-up/employee")
    suspend fun signUpEmployee(@Body request: User): Response<User>

    /**
     * Lists every employee profile. Used by the post-login sequential sync to locate the
     * worker's own row by matching [User.user_account_id] against the persisted account id,
     * then extracting [User.employee_profile_id].
     */
    @GET("api/v1/employee-profile")
    suspend fun getEmployeeProfiles(): Response<List<User>>
}
