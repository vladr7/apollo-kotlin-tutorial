package com.example.rocketreserver

import android.content.Context
import android.util.Log
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.ApolloResponse
import com.apollographql.apollo3.api.Error
import com.apollographql.apollo3.api.Operation
import com.apollographql.apollo3.exception.ApolloException
import com.apollographql.apollo3.network.okHttpClient
import kotlinx.coroutines.delay
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

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


@OptIn(ExperimentalContracts::class)
inline fun <D : Operation.Data, R> ApolloResponse<D>.fold(
    onException: (exception: ApolloException) -> R,
    onErrors: ApolloResponse<D>.(errors: List<Error>) -> R,
    onSuccess: ApolloResponse<D>.(data: D) -> R,
): R {
    contract {
        callsInPlace(onException, InvocationKind.AT_MOST_ONCE)
        callsInPlace(onErrors, InvocationKind.AT_MOST_ONCE)
        callsInPlace(onSuccess, InvocationKind.AT_MOST_ONCE)
    }

    return when {
        exception != null -> onException(exception!!)
        hasErrors() -> onErrors(errors!!)
        else -> onSuccess(data!!)
    }
}
