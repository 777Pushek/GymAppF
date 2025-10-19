package com.example.gymappfrontendui.network

import okhttp3.Authenticator
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory


class TokenAuthenticator(
    private val tokenManager: TokenManager,
    private val cookieJar: RefreshCookieJar
) : Authenticator {
    override fun authenticate(route: Route?, response: Response): Request? {
        if (response.request.header("Authorization") != null && responseCount(response) >= 2) {
            return null
        }

        val newToken = refreshAccessToken() ?: return null
        tokenManager.saveToken(newToken)

        return response.request.newBuilder()
            .header("Authorization", "Bearer $newToken")
            .build()
    }

    private fun refreshAccessToken(): String? {
        return try {
            val retrofit = Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8000/api/")
                .client(
                    OkHttpClient.Builder()
                        .cookieJar(cookieJar)
                        .build()
                )
                .addConverterFactory(MoshiConverterFactory.create())
                .build()

            val service = retrofit.create(ApiService::class.java)
            val response = service.refreshToken().execute()
            if (response.isSuccessful) {
                response.body()?.token
            } else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun responseCount(response: Response): Int {
        var result = 1
        var priorResponse = response.priorResponse
        while (priorResponse != null) {
            result++
            priorResponse = priorResponse.priorResponse
        }
        return result
    }
}