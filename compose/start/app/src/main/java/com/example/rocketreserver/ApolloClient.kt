package com.example.rocketreserver

import android.util.Log
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.network.okHttpClient
import com.apollographql.apollo3.network.ws.GraphQLWsProtocol
import com.apollographql.apollo3.network.ws.WebSocketNetworkTransport
import kotlinx.coroutines.delay
import okhttp3.OkHttpClient

val apolloClient = ApolloClient.Builder()
    .serverUrl("https://apollo-fullstack-tutorial.herokuapp.com/graphql")
    .webSocketReopenWhen { throwable, attempt ->
        Log.d("Apollo", "WebSocket got disconnected, reopening after a delay", throwable)
        delay(attempt * 1000)
        true
    }
    .okHttpClient(
        OkHttpClient.Builder()
            .addInterceptor(AuthorizationInterceptor())
            .build()
    )
    .build()

