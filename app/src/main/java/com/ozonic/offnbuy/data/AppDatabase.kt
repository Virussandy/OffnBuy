package com.ozonic.offnbuy.data

import android.content.Context
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
import com.ozonic.offnbuy.model.NotifiedDeal
import com.ozonic.offnbuy.model.SupportedStore
import com.ozonic.offnbuy.model.UserProfile
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
@Database(entities = [DealItem::class, GeneratedLink::class, FavoriteDeal::class, SupportedStore::class, UserProfile::class, NotifiedDeal::class], version = 7, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dealDao(): DealDao
    abstract fun generatedLinkDao(): GeneratedLinkDao
    abstract fun favoriteDealDao(): FavoriteDealDao
    abstract fun supportedStoreDao(): SupportedStoreDao
    abstract fun userProfileDao(): UserProfileDao

    abstract fun notifiedDealDao(): NotifiedDealDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
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


        val MIGRATION_5_6 = object : Migration(5, 6){
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `user_profile` (`uid` TEXT NOT NULL, `name` TEXT, `email` TEXT, `phone` TEXT, `profilePic` TEXT, PRIMARY KEY(`uid`))")
            }
        }

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // This SQL creates the new table based on the NotifiedDeal entity
                db.execSQL("CREATE TABLE IF NOT EXISTS `notified_deals` (`deal_id` TEXT NOT NULL, `isSeen` INTEGER NOT NULL, `timestamp` INTEGER NOT NULL, `deal_deal_id` TEXT NOT NULL, `deal_discount` TEXT, `deal_image` TEXT, `deal_originalPrice` TEXT, `deal_posted_on` TEXT, `deal_price` TEXT, `deal_redirectUrl` TEXT, `deal_store` TEXT, `deal_title` TEXT, `deal_url` TEXT, PRIMARY KEY(`deal_id`))")
            }
        }
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "offnbuy_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}