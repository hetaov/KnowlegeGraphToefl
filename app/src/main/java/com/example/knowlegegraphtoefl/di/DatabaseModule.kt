package com.example.knowlegegraphtoefl.di

import android.content.Context
import androidx.room.Room
import com.example.knowlegegraphtoefl.data.local.AppDatabase
import com.example.knowlegegraphtoefl.data.local.SentenceDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "toefl_database"
        ).build()
    }

    @Provides
    fun provideSentenceDao(database: AppDatabase): SentenceDao {
        return database.sentenceDao()
    }
}
