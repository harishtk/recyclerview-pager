package com.example.trainingwheel01.data.source.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.trainingwheel01.data.entity.UserData
import com.example.trainingwheel01.data.entity.UserRemoteKeys
import com.example.trainingwheel01.data.source.local.dao.RemoteKeysDao
import com.example.trainingwheel01.data.source.local.dao.UserDataDao
import dagger.hilt.android.qualifiers.ApplicationContext

@Database(entities = [UserData::class, UserRemoteKeys::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun usersDao(): UserDataDao
    abstract fun userRemoteKeysDao(): RemoteKeysDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(@ApplicationContext context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE
                    ?: buildDatabase(context).also { INSTANCE = it }
            }

        private fun buildDatabase(@ApplicationContext context: Context) =
            Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java, "Users.db"
            )
                .build()
    }
}