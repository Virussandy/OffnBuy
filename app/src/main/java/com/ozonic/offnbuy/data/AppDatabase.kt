package com.ozonic.offnbuy.data

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ozonic.offnbuy.model.DealItem
import com.ozonic.offnbuy.model.FavoriteDeal
import com.ozonic.offnbuy.model.GeneratedLink
import com.ozonic.offnbuy.model.SupportedStore
import java.util.Date


class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}
@Database(entities = [DealItem::class, GeneratedLink::class, FavoriteDeal::class, SupportedStore::class], version = 5, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dealDao(): DealDao
    abstract fun generatedLinkDao(): GeneratedLinkDao
    abstract fun favoriteDealDao(): FavoriteDealDao
    abstract fun supportedStoreDao(): SupportedStoreDao

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

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE `favorite_deals` (`deal_id` TEXT NOT NULL, `userId` TEXT NOT NULL, `addedAt` INTEGER NOT NULL, PRIMARY KEY(`deal_id`, `userId`))")
                db.execSQL("CREATE TABLE `generated_links_new` (`url` TEXT NOT NULL, `userId` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, PRIMARY KEY(`url`, `userId`))")
                // 2. Drop the old table (we cannot migrate old links as they weren't tied to a user)
                db.execSQL("DROP TABLE `generated_links`")
                // 3. Rename the new table to the original name
                db.execSQL("ALTER TABLE `generated_links_new` RENAME TO `generated_links`")
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `supported_stores` (`name` TEXT NOT NULL, `logoUrl` TEXT NOT NULL, PRIMARY KEY(`name`))")
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
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}