package com.swiftflow.domain.model

enum class UserRole {
    SALES,
    SUPERVISOR
}

data class User(
    val id: Int,
    val username: String,
    val role: UserRole,
    val initial: String,
    val isActive: Boolean,
    val createdAt: String
)

data class LoginResponse(
    val token: String,
    val user: User
)
