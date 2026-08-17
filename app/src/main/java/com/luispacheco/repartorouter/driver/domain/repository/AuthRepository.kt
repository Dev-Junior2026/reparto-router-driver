package com.luispacheco.repartorouter.driver.domain.repository

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<Unit>
}