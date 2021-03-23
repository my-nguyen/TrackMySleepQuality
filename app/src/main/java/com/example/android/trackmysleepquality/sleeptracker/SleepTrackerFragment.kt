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

package com.example.android.trackmysleepquality.sleeptracker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.android.trackmysleepquality.R
import com.example.android.trackmysleepquality.database.SleepDatabase
import com.example.android.trackmysleepquality.databinding.FragmentSleepTrackerBinding
import com.google.android.material.snackbar.Snackbar

/**
 * A fragment with buttons to record start and end times for sleep, which are saved in
 * a database. Cumulative data is displayed in a simple scrollable TextView.
 * (Because we have not learned about RecyclerView yet.)
 */
class SleepTrackerFragment : Fragment() {

    /**
     * Called when the Fragment is ready to display content to the screen.
     *
     * This function uses DataBindingUtil to inflate R.layout.fragment_sleep_quality.
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentSleepTrackerBinding.inflate(inflater, container, false)

        // get a reference to the application context, to pass to the view-model factory provider
        val application = requireNotNull(activity).application

        // get a reference to the data source via a reference to the DAO
        val dataSource = SleepDatabase.getInstance(application).sleepDatabaseDao

        // create an instance of the viewModelFactory
        val viewModelFactory = SleepTrackerViewModelFactory(dataSource, application)

        // get a reference to the SleepTrackerViewModel
        val sleepTrackerViewModel = ViewModelProvider(this, viewModelFactory).get(SleepTrackerViewModel::class.java)

        binding.startButton.setOnClickListener {
            sleepTrackerViewModel.onStartTracking()
        }

        binding.stopButton.setOnClickListener {
            sleepTrackerViewModel.onStopTracking()
        }

        binding.clearButton.setOnClickListener {
            sleepTrackerViewModel.onClear()
        }

        // observe changes to nightString and update the TextView accordingly
        sleepTrackerViewModel.nightsString.observe(viewLifecycleOwner, Observer {
            binding.textview.text = it.toString()
        })

        //  observe navigateToSleepQuality: when a new value is set, navigate to SleepQualityFragment
        sleepTrackerViewModel.navigateToSleepQuality.observe(viewLifecycleOwner, Observer {
            it?.let {
                // navigate and pass along the ID of the current night
                val directions = SleepTrackerFragmentDirections.actionSleepTrackerFragmentToSleepQualityFragment(it.nightId)
                findNavController().navigate(directions)
                sleepTrackerViewModel.doneNavigating()
            }
        })

        sleepTrackerViewModel.startButtonVisible.observe(viewLifecycleOwner, Observer {
            binding.startButton.isEnabled = it
        })
        sleepTrackerViewModel.stopButtonVisible.observe(viewLifecycleOwner, Observer {
            binding.stopButton.isEnabled = it
        })
        sleepTrackerViewModel.clearButtonVisible.observe(viewLifecycleOwner, Observer {
            binding.clearButton.isEnabled = it!!
        })

        sleepTrackerViewModel.showSnackBarEvent.observe(viewLifecycleOwner, Observer {
            if (it == true) {
                // display the snackbar
                val activity = requireActivity().findViewById<View>(android.R.id.content)
                Snackbar.make(activity, getString(R.string.cleared_message), Snackbar.LENGTH_SHORT).show()
                // immediately reset the event
                sleepTrackerViewModel.doneShowingSnackbar()
            }
        })
        return binding.root
    }
}
