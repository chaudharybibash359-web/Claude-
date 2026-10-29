package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        ConversationEntity::class,
        ChatMessageEntity::class,
        ArtifactEntity::class,
        ProjectEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class ClaudeDatabase : RoomDatabase() {
    abstract fun claudeDao(): ClaudeDao

    companion object {
        @Volatile
        private var INSTANCE: ClaudeDatabase? = null

        fun getInstance(context: Context): ClaudeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ClaudeDatabase::class.java,
                    "claude_premium_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
