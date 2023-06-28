@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.rocketreserver

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.apollographql.apollo3.api.ApolloResponse
import com.apollographql.apollo3.api.Optional
import kotlin.random.Random

@Composable
fun LaunchList(onLaunchClick: (launchId: String) -> Unit) {
    var cursor: String? by remember { mutableStateOf(null) }
    var response: ApolloResponse<LaunchListQuery.Data>? by remember { mutableStateOf(null) }
    var launchList by remember { mutableStateOf(emptyList<LaunchListQuery.Launch>()) }

    LaunchedEffect(
        key1 = cursor,
    ) {
        response = apolloClient.query(
            LaunchListQuery(
                cursor = Optional.present(cursor)
            )
        ).execute()
        launchList = response?.data?.launches?.launches?.filterNotNull() ?: emptyList()
        Log.d(
            "LaunchList",
            "Success ${response?.data}"
        )
    }

    LazyColumn {
        items(launchList) { launch ->
            LaunchItem(
                launch = launch,
                onClick = onLaunchClick
            )
        }
        item {
            if (response?.data?.launches?.hasMore == true) {
                LoadingItem()
                cursor = response?.data?.launches?.cursor
            }
        }
    }
}

@Composable
private fun LaunchItem(launch: LaunchListQuery.Launch, onClick: (launchId: String) -> Unit) {
    var color: Color? by remember { mutableStateOf(null) }
    LaunchedEffect(
        key1 =  launch.id,
    ) {
        val isBooked = apolloClient.query(LaunchBookedQuery(launchId = launch.id)).execute().data?.launch?.isBooked ?: false
        color = if (isBooked) Color(red = 0.8f, green = 0.0f, blue = 0.0f, alpha = 0.4f) else
            Color(red = 0.0f, green = 0.0f, blue = 0.8f, alpha = 0.4f)
    }
    ListItem(
        colors = ListItemDefaults.colors(
            containerColor = color ?: Color.Transparent,
        ),
        modifier = Modifier
            .clickable { onClick(launch.id) },
        headlineText = {
            // Mission name
            Text(text = launch.mission?.name ?: "")
        },
        supportingText = {
            // Site
            Text(text = launch.site ?: "")
        },
        leadingContent = {
            // Mission patch
            AsyncImage(
                modifier = Modifier.size(
                    68.dp,
                    68.dp
                ),
                model = launch.mission?.missionPatch,
                placeholder = painterResource(R.drawable.ic_placeholder),
                error = painterResource(R.drawable.ic_placeholder),
                contentDescription = "Mission patch"
            )
        }
    )
}

@Composable
private fun LoadingItem() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        CircularProgressIndicator()
    }
}
