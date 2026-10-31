package com.quimia.android.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.quimia.android.data.local.preferences.AccessibilityPreferencesDao
import com.quimia.android.data.local.preferences.AccessibilityPreferencesEntity

@Database(entities = [AccessibilityPreferencesEntity::class], version = 2, exportSchema = true)
internal abstract class QuimiaDatabase : RoomDatabase() {
    abstract fun accessibilityPreferences(): AccessibilityPreferencesDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE accessibility_preferences ADD COLUMN mixtureNotifications INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE accessibility_preferences ADD COLUMN shelfReminders INTEGER NOT NULL DEFAULT 0")
            }
        }
        @Volatile private var instance: QuimiaDatabase? = null

        fun getInstance(context: Context): QuimiaDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(context.applicationContext, QuimiaDatabase::class.java, "quimia.db")
                .addMigrations(MIGRATION_1_2)
                .build().also { instance = it }
        }
    }
}
