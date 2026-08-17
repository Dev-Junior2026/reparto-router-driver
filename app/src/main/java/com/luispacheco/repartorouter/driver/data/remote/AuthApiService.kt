package com.luispacheco.repartorouter.driver.data.remote

import com.luispacheco.repartorouter.driver.data.remote.dto.LoginRequest
import com.luispacheco.repartorouter.driver.data.remote.dto.LoginResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>
}