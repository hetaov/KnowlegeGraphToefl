package com.example.knowlegegraphtoefl.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [SentenceEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun sentenceDao(): SentenceDao
}
