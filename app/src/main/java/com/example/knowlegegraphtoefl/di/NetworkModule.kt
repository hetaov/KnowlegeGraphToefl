package com.example.knowlegegraphtoefl.di

import com.google.ai.client.generativeai.GenerativeModel
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideGenerativeModel(): GenerativeModel {
        // NOTE: In a real app, don't hardcode the API key. 
        // Use BuildConfig or a secure way to fetch it.
        return GenerativeModel(
            modelName = "gemini-1.5-flash",
            apiKey = "YOUR_API_KEY_HERE"
        )
    }
}
