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

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import com.example.android.trackmysleepquality.database.SleepDatabaseDao
import com.example.android.trackmysleepquality.database.SleepNight
import com.example.android.trackmysleepquality.formatNights
import kotlinx.coroutines.launch

/**
 * ViewModel for SleepTrackerFragment.
 */
class SleepTrackerViewModel(val database: SleepDatabaseDao, application: Application) : AndroidViewModel(application) {
    // hold the current night; type MutableLiveData to observe the data and change it
    private var tonight = MutableLiveData<SleepNight?>()

    // hold all the nights from the database
    private val nights = database.getAllNights()
    // transform nights into a nightsString
    val nightsString = Transformations.map(nights) {
        formatNights(it, application.resources)
    }

    init {
        initializeTonight()
    }

    private fun initializeTonight() {
        // start a coroutine in the ViewModelScope
        viewModelScope.launch {
            // fetch the value for tonight from the database
            tonight.value = getTonightFromDatabase()
        }
    }

    private suspend fun getTonightFromDatabase(): SleepNight? {
        // get tonight (the newest night) from the database
        var night = database.getTonight()
        // If the start and end times are not the same, meaning that the night has already been completed, return null
        if (night?.endTimeMilli != night?.startTimeMilli) {
            night = null
        }
        // Otherwise, return the night
        return night
    }

    // the click handler for the Start button
    fun onStartTracking() {
        // launch a coroutine in the viewModelScope, because you need this result to continue and
        // update the UI
        viewModelScope.launch {
            // create a new SleepNight, which captures the current time as the start time
            val newNight = SleepNight()
            // insert newNight into the database
            insert(newNight)
            // assign newNight to tonight
            tonight.value = getTonightFromDatabase()
        }
    }

    fun onStopTracking() {
        // Launch a coroutine in the viewModelScope
        viewModelScope.launch {
            val oldNight = tonight.value ?: return@launch
            // If the end time hasn't been set yet, set the endTimeMilli to the current system time
            // and call update() with the night data.
            oldNight.endTimeMilli = System.currentTimeMillis()
            update(oldNight)
        }
    }

    fun onClear() {
        viewModelScope.launch {
            clear()
            tonight.value = null
        }
    }

    private suspend fun insert(night: SleepNight) {
        // use the DAO to insert night into the database. this Room coroutine uses Dispatchers.IO,
        // so this will not happen on the main thread.
        database.insert(night)
    }

    private suspend fun update(night: SleepNight) {
        database.update(night)
    }

    private suspend fun clear() {
        database.clear()
    }
}

