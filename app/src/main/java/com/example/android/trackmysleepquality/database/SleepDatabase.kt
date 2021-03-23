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

package com.example.android.trackmysleepquality.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities=[SleepNight::class], version=1, exportSchema=false)
abstract class SleepDatabase : RoomDatabase() {

    abstract val sleepDatabaseDao: SleepDatabaseDao

    // companion object allow clients to access the methods for creating or getting the database
    // without instantiating the class
    companion object {
        // INSTANCE variable keeps a reference to the database
        // a Volatile variable is never cached, and all writes and reads will be done to and from
        // main memory. this makes sure the value of INSTANCE is always up-to-date and the same to
        // all execution threads. it means that changes made by one thread to INSTANCE are visible
        // to all other threads immediately
        @Volatile
        private var INSTANCE: SleepDatabase? = null

        fun getInstance(context: Context): SleepDatabase {
            // multiple threads can potentially call getInstance() at the same time. a synchronized
            // block ensures that only one thread of execution can enter the block, so that the
            // database only gets initialized once
            synchronized(this) {
                var instance = INSTANCE
                if (instance == null) {
                    instance = Room.databaseBuilder(context.applicationContext, SleepDatabase::class.java, "sleep_database")
                            .fallbackToDestructiveMigration()
                            .build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}