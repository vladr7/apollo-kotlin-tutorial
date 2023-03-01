package com.example.rocketreserver

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.apollographql.apollo3.api.Optional
import com.example.rocketreserver.databinding.LaunchListFragmentBinding
import kotlinx.coroutines.channels.Channel

class LaunchListFragment : Fragment() {
    private lateinit var binding: LaunchListFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = LaunchListFragmentBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val launches = mutableListOf<LaunchListQuery.Launch>()
        val adapter = LaunchListAdapter(launches)
        binding.launches.layoutManager = LinearLayoutManager(requireContext())
        binding.launches.adapter = adapter

        val channel = Channel<Unit>(Channel.CONFLATED)

        // Send a first item to do the initial load else the list will stay empty forever
        channel.trySend(Unit)
        adapter.onEndOfListReached = {
            channel.trySend(Unit)
        }

        lifecycleScope.launchWhenResumed {
            var cursor: String? = null
            for (item in channel) {
                val data =
                    apolloClient(requireContext()).query(LaunchListQuery(Optional.Present(cursor)))
                        .execute()
                        .fold(
                            onException = { e ->
                                Log.d("LaunchList", "Network error", e)
                                return@launchWhenResumed
                            },
                            onErrors = { errors ->
                                Log.d("LaunchList", "Backend error: ${errors.map { it.message }}")
                                return@launchWhenResumed
                            },
                            onSuccess = { it }
                        )

                val newLaunches = data.launches.launches.filterNotNull()
                launches.addAll(newLaunches)
                adapter.notifyDataSetChanged()

                cursor = data.launches.cursor
                if (!data.launches.hasMore) {
                    break
                }
            }

            adapter.onEndOfListReached = null
            channel.close()
        }

        adapter.onItemClicked = { launch ->
            findNavController().navigate(
                LaunchListFragmentDirections.openLaunchDetails(launchId = launch.id)
            )
        }
    }
}
