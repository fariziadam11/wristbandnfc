package com.gbs.wristbandnfc.di

import android.content.Context
import com.gbs.wristbandnfc.data.util.DemoModeManager
import com.gbs.wristbandnfc.util.SessionManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideSessionManager(
        @ApplicationContext context: Context
    ): SessionManager {
        return SessionManager(context)
    }

    @Provides
    @Singleton
    fun provideDemoModeManager(
        @ApplicationContext context: Context
    ): DemoModeManager {
        return DemoModeManager(context)
    }
}
