/*
 * Copyright 2019, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.trackmysleepquality.sleepquality

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.android.trackmysleepquality.database.SleepDatabase
import com.example.android.trackmysleepquality.databinding.FragmentSleepQualityBinding

/**
 * Fragment that displays a list of clickable icons,
 * each representing a sleep quality rating.
 * Once the user taps an icon, the quality is set in the current sleepNight
 * and the database is updated.
 */
class SleepQualityFragment : Fragment() {

    private lateinit var sleepQualityViewModel: SleepQualityViewModel
    /**
     * Called when the Fragment is ready to display content to the screen.
     *
     * This function uses DataBindingUtil to inflate R.layout.fragment_sleep_quality.
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentSleepQualityBinding.inflate(inflater, container, false)

        val application = requireNotNull(this.activity).application
        // get the arguments that came with the navigation
        val arguments = SleepQualityFragmentArgs.fromBundle(requireArguments())
        // get the dataSource.
        val dataSource = SleepDatabase.getInstance(application).sleepDatabaseDao
        // Create a factory, passing in the dataSource and the sleepNightKey
        val viewModelFactory = SleepQualityViewModelFactory(arguments.sleepNightKey, dataSource)
        // Get a ViewModel reference
        val provider = ViewModelProvider(this, viewModelFactory)
        sleepQualityViewModel = provider.get(SleepQualityViewModel::class.java)

        sleepQualityViewModel.navigateToSleepTracker.observe(viewLifecycleOwner, Observer {
            if (it == true) {
                val directions = SleepQualityFragmentDirections.actionSleepQualityFragmentToSleepTrackerFragment()
                this.findNavController().navigate(directions)
                sleepQualityViewModel.doneNavigating()
            }
        })

        return binding.root
    }

    fun clickListener(view: View) {
        sleepQualityViewModel.onSetSleepQuality(5)
    }
}
