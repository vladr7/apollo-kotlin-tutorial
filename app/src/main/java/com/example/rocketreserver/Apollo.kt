package com.example.rocketreserver

import android.content.Context
import android.util.Log
import com.apollographql.apollo3.ApolloCall
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.ApolloResponse
import com.apollographql.apollo3.api.Operation
import com.apollographql.apollo3.exception.ApolloException
import com.apollographql.apollo3.network.okHttpClient
import kotlinx.coroutines.delay
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response

private var instance: ApolloClient? = null

fun apolloClient(context: Context): ApolloClient {
    if (instance != null) {
        return instance!!
    }

    val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(AuthorizationInterceptor(context))
        .build()

    instance = ApolloClient.Builder()
        .serverUrl("https://apollo-fullstack-tutorial.herokuapp.com/graphql")
        .webSocketServerUrl("wss://apollo-fullstack-tutorial.herokuapp.com/graphql")
        .webSocketReopenWhen { _, attempt ->
            delay(attempt * 1000)
            Log.d("WebSocket", "Reconnecting attempt=$attempt...")
            true
        }
        .okHttpClient(okHttpClient)
        .build()

    return instance!!
}

private class AuthorizationInterceptor(val context: Context) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .addHeader("Authorization", User.getToken(context) ?: "")
            .build()

        return chain.proceed(request)
    }
}


fun <D : Operation.Data> ApolloResponse<D>.toResult(): Result<D> {
    return when {
        exception != null -> Result.failure(exception!!)
        hasErrors() -> Result.failure(ApolloException("The response has errors: $errors")) // maybe a specific exception with the errors as a field?
        data == null -> Result.failure(ApolloException("The server did not return any data"))
        else -> Result.success(data!!)
    }
}

suspend fun <D : Operation.Data> ApolloCall<D>.toResult() = execute().toResult()
