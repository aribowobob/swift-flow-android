package com.swiftflow.domain.model

import com.google.gson.annotations.SerializedName

enum class UserRole {
    @SerializedName("Sales")
    SALES,

    @SerializedName("Supervisor")
    SUPERVISOR
}

data class User(
    val id: Int,
    val username: String,
    val role: UserRole,
    val initial: String,
    @SerializedName("is_active")
    val isActive: Boolean,
    @SerializedName("created_at")
    val createdAt: String
)

data class LoginResponse(
    val token: String,
    val user: User
)
