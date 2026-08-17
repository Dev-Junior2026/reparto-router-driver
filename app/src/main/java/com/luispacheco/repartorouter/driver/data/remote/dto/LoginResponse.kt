package com.luispacheco.repartorouter.driver.data.remote.dto

data class LoginResponse(
    val token: String,
    val choferId: Long,
    val nombre: String,
    val email: String
)