package com.example.knowlegegraphtoefl.di

import com.example.knowlegegraphtoefl.data.repository.SentenceRepositoryImpl
import com.example.knowlegegraphtoefl.domain.repository.SentenceRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindSentenceRepository(
        impl: SentenceRepositoryImpl
    ): SentenceRepository
}
