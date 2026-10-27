package com.quimia.android.data.auth

import android.content.Context
import com.quimia.android.BuildConfig

/** One rotation mutex per process, including multiple Activity/ViewModel instances. */
object AuthRepositoryProvider {
    @Volatile private var instance: AuthRepository? = null

    fun get(context: Context): AuthRepository = instance ?: synchronized(this) {
        instance ?: AuthRepository(
            AuthApiFactory.create(BuildConfig.AUTH_BASE_URL),
            AndroidSessionStore(context.applicationContext),
        ).also { instance = it }
    }
}
