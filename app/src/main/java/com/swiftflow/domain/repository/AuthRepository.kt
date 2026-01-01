package com.swiftflow.domain.repository

import com.swiftflow.domain.model.LoginResponse
import com.swiftflow.utils.Resource
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun login(username: String, password: String): Flow<Resource<LoginResponse>>
    suspend fun logout()
    fun isLoggedIn(): Flow<Boolean>
}
