package com.example.medicare.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.medicare.data.local.dao.MedicamentoDao
import com.example.medicare.data.local.entities.MedicamentoEntity

@Database(entities = [MedicamentoEntity::class], version = 3, exportSchema = false)
abstract class MedicareDatabase : RoomDatabase() {
    abstract fun medicamentoDao(): MedicamentoDao

    companion object {
        @Volatile
        private var INSTANCE: MedicareDatabase? = null

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE medicamentos_locales ADD COLUMN fechaRegistro INTEGER NOT NULL DEFAULT 0")
            }
        }
        fun getDatabase(context: Context): MedicareDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MedicareDatabase::class.java,
                    "medicare_offline_db"
                )
                    .addMigrations(MIGRATION_2_3)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}