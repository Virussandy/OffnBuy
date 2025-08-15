package com.ozonic.offnbuy.data

import android.content.Context
import android.util.Log // <-- Import Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ozonic.offnbuy.model.DealItem
import com.ozonic.offnbuy.model.GeneratedLink

@Database(entities = [DealItem::class, GeneratedLink::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dealDao(): DealDao
    abstract fun generatedLinkDao(): GeneratedLinkDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // This log is our proof that the migration is running
                Log.d("DB_MIGRATION", "MIGRATION FROM 1 TO 2 IS RUNNING")
                db.execSQL("CREATE TABLE `generated_links` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `url` TEXT NOT NULL)")
            }
        }

        // New migration to add the unique index
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // SQL command to create a unique index
                db.execSQL("CREATE UNIQUE INDEX `index_generated_links_url` ON `generated_links` (`url`)")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this){
                Log.d("DB_SETUP", "Database instance is null, creating new one.") // <-- Log 1
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "offnbuy_database"
                )
                    .addCallback(object : Callback() { // <-- Log 2 (The most important one)
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            Log.d("DB_SETUP", "Database has been CREATED. Version: ${db.version}")
                        }
                        override fun onOpen(db: SupportSQLiteDatabase) {
                            super.onOpen(db)
                            Log.d("DB_SETUP", "Database has been OPENED. Version: ${db.version}")
                        }
                    })
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}