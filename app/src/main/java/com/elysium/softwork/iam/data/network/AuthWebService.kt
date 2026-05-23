package com.elysium.softwork.iam.data.network

import com.elysium.softwork.iam.domain.model.User
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Retrofit contract for IAM endpoints. The same [User] data class carries both the request
 * body and the response payload — no DTO/assembler boilerplate. Different endpoints fill
 * different subsets of [User] (see the model KDoc).
 */
interface AuthWebService {

    /**
     * Authenticates an existing employee. Send a [User] with [User.email] and [User.password]
     * populated; the server response includes [User.id], [User.username], [User.role], and
     * [User.token].
     */
    @POST("auth/login")
    suspend fun login(@Body credentials: User): Response<User>

    /**
     * Registers a new employee. Send a [User] with [User.username], [User.email],
     * [User.password], and [User.role] populated.
     */
    @POST("auth/register")
    suspend fun register(@Body user: User): Response<User>

    /**
     * Completes registration for an account that authenticated through Google's identity
     * provider. The caller has already obtained an OAuth identity, so only [User.username]
     * and [User.role] are required from the device — the server resolves email + identity
     * server-side from the bearer token used on this call.
     */
    @POST("auth/google/register")
    suspend fun registerWithGoogle(@Body user: User): Response<User>
}
