package com.handbook.app.core.di

import android.app.Application
import com.handbook.app.core.di.AppDependencies
import com.handbook.app.core.persistence.DefaultPersistentStore
import com.handbook.app.core.persistence.PersistentStore
import com.handbook.app.core.util.AppForegroundObserver

class AppDependenciesProvider(private val application: Application) : AppDependencies.Provider {
    override fun provideAppForegroundObserver(): AppForegroundObserver {
        return AppForegroundObserver()
    }

    override fun providePersistentStore(): PersistentStore {
        return DefaultPersistentStore.getInstance(application)
    }


}