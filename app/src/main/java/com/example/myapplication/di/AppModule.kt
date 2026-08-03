package com.example.myapplication.di

import android.content.Context
import androidx.room.Room
import com.example.myapplication.data.local.database.AppDatabase
import com.example.myapplication.data.local.preferences.PreferencesRepository
import com.example.myapplication.data.local.preferences.PreferencesRepositoryImpl
import com.example.myapplication.data.remote.ApiService
import com.example.myapplication.data.remote.NetworkModule
import com.example.myapplication.data.repository.AuthRepository
import com.example.myapplication.data.repository.AuthRepositoryImpl
import com.example.myapplication.data.repository.UserRepository
import com.example.myapplication.data.repository.UserRepositoryImpl
import com.example.myapplication.data.ads.AdsManager
import com.example.myapplication.data.ads.AdsManagerImpl
import com.example.myapplication.domain.repository.AuthRepository as DomainAuthRepository
import com.example.myapplication.domain.repository.UserRepository as DomainUserRepository
import com.example.myapplication.domain.usecase.GetCurrentUserUseCase
import com.example.myapplication.domain.usecase.LoginUseCase
import com.example.myapplication.domain.usecase.LogoutUseCase
import com.example.myapplication.domain.usecase.RegisterUseCase
import com.example.myapplication.presentation.viewmodel.AuthViewModel
import com.example.myapplication.presentation.viewmodel.HomeViewModel
import com.example.myapplication.presentation.viewmodel.SettingsViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ActivityRetainedScoped
import dagger.hilt.android.scopes.Singleton
import javax.inject.Inject
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "app_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun providePreferencesRepository(@ApplicationContext context: Context): PreferencesRepository {
        return PreferencesRepositoryImpl(context)
    }

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    @Provides
    @Singleton
    fun provideApiService(): ApiService {
        return NetworkModule.createApiService()
    }

    @Provides
    @Singleton
    fun provideAdsManager(@ApplicationContext context: Context, preferencesRepository: PreferencesRepository): AdsManager {
        return AdsManagerImpl(context, preferencesRepository)
    }
}

@Module
@InstallIn(ApplicationComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): DomainAuthRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(impl: UserRepositoryImpl): DomainUserRepository
}

@Module
@InstallIn(ViewModelComponent::class)
abstract class UseCaseModule {

    @Binds
    abstract fun bindLoginUseCase(impl: LoginUseCase): LoginUseCase

    @Binds
    abstract fun bindRegisterUseCase(impl: RegisterUseCase): RegisterUseCase

    @Binds
    abstract fun bindLogoutUseCase(impl: LogoutUseCase): LogoutUseCase

    @Binds
    abstract fun bindGetCurrentUserUseCase(impl: GetCurrentUserUseCase): GetCurrentUserUseCase
}

@Module
@InstallIn(ActivityRetainedComponent::class)
abstract class ViewModelModule {

    @Binds
    @ActivityRetainedScoped
    abstract fun bindAuthViewModel(impl: AuthViewModel): AuthViewModel

    @Binds
    @ActivityRetainedScoped
    abstract fun bindHomeViewModel(impl: HomeViewModel): HomeViewModel

    @Binds
    @ActivityRetainedScoped
    abstract fun bindSettingsViewModel(impl: SettingsViewModel): SettingsViewModel
}