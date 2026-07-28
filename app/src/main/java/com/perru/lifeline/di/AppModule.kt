package com.perru.lifeline.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.perru.lifeline.data.repository.AuthRepositoryImpl
import com.perru.lifeline.data.repository.RequestRepositoryImpl
import com.perru.lifeline.domain.repository.AuthRepository
import com.perru.lifeline.domain.repository.RequestRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseDatabase(): FirebaseDatabase =
        FirebaseDatabase.getInstance().apply {
            // Keeps the users/requests/pledges trees available offline and syncs
            // on reconnect — RTDB's equivalent of Firestore's built-in offline cache.
            setPersistenceEnabled(true)
        }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindRequestRepository(impl: RequestRepositoryImpl): RequestRepository
}