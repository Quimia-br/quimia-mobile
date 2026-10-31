package com.quimia.android.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.quimia.android.data.local.preferences.AccessibilityPreferencesDao
import com.quimia.android.data.local.preferences.AccessibilityPreferencesEntity

@Database(entities = [AccessibilityPreferencesEntity::class], version = 1, exportSchema = true)
internal abstract class QuimiaDatabase : RoomDatabase() {
    abstract fun accessibilityPreferences(): AccessibilityPreferencesDao

    companion object {
        @Volatile private var instance: QuimiaDatabase? = null

        fun getInstance(context: Context): QuimiaDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(context.applicationContext, QuimiaDatabase::class.java, "quimia.db")
                .build().also { instance = it }
        }
    }
}
