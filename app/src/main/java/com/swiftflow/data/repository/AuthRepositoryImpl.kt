package com.swiftflow.data.repository

import com.swiftflow.data.remote.api.AuthApi
import com.swiftflow.data.remote.api.LoginRequest
import com.swiftflow.domain.model.LoginResponse
import com.swiftflow.domain.repository.AuthRepository
import com.swiftflow.utils.Resource
import com.swiftflow.utils.TokenManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApi,
    private val tokenManager: TokenManager
) : AuthRepository {

    override suspend fun login(username: String, password: String): Flow<Resource<LoginResponse>> = flow {
        try {
            emit(Resource.Loading())
            val response = authApi.login(LoginRequest(username, password))
            tokenManager.saveToken(response.token)
            tokenManager.saveUserInfo(response.user.id, response.user.username)
            emit(Resource.Success(response))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "An unexpected error occurred"))
        }
    }

    override suspend fun logout() {
        tokenManager.clearToken()
    }

    override fun isLoggedIn(): Flow<Boolean> {
        return tokenManager.getToken().map { it != null }
    }
}
