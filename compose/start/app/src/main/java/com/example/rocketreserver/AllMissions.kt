package com.example.rocketreserver

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.apollographql.apollo3.api.Optional

@Composable
fun AllMissionsScreen() {
    var launchList by remember { mutableStateOf(emptyList<GetMissionsQuery.Launch>()) }

    LaunchedEffect(
        key1 = Unit,
    ) {
        val query = apolloClient.query(GetMissionsQuery(pageSize = Optional.present(5))).execute()
        launchList = query.data?.launches?.launches?.filterNotNull() ?: emptyList()
    }

    LazyColumn {
        items(launchList) { launch ->
            BasicLaunchItem(missionName = launch.mission?.name ?: "Loading..")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BasicLaunchItem(missionName: String) {
    ListItem(headlineText = {
        Text(text = missionName)
    })
}
