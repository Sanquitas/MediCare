package com.example.medicare.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.medicare.data.local.dao.MedicamentoDao
import com.example.medicare.data.local.entities.MedicamentoEntity

@Database(entities = [MedicamentoEntity::class], version = 2, exportSchema = false)
abstract class MedicareDatabase : RoomDatabase() {
    abstract fun medicamentoDao(): MedicamentoDao

    companion object {
        @Volatile
        private var INSTANCE: MedicareDatabase? = null
        fun getDatabase(context: Context): MedicareDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MedicareDatabase::class.java,
                    "medicare_offline_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}