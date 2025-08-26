package com.ozonic.offnbuy.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ozonic.offnbuy.data.local.dao.DealDao
import com.ozonic.offnbuy.data.local.dao.FavoriteDealDao
import com.ozonic.offnbuy.data.local.dao.GeneratedLinkDao
import com.ozonic.offnbuy.data.local.dao.NotifiedDealDao
import com.ozonic.offnbuy.data.local.dao.SupportedStoreDao
import com.ozonic.offnbuy.data.local.dao.UserProfileDao
import com.ozonic.offnbuy.data.local.model.DealEntity
import com.ozonic.offnbuy.data.local.model.FavoriteDealEntity
import com.ozonic.offnbuy.data.local.model.GeneratedLinkEntity
import com.ozonic.offnbuy.data.local.model.NotifiedDealEntity
import com.ozonic.offnbuy.data.local.model.SupportedStoreEntity
import com.ozonic.offnbuy.data.local.model.UserProfileEntity
import java.util.Date

/**
 * The Room database for the application.
 */
@Database(
    entities = [
        DealEntity::class,
        GeneratedLinkEntity::class,
        FavoriteDealEntity::class,
        SupportedStoreEntity::class,
        UserProfileEntity::class,
        NotifiedDealEntity::class
    ],
    version = 7,
    exportSchema = false
)
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

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE `generated_links` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `url` TEXT NOT NULL)")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE UNIQUE INDEX `index_generated_links_url` ON `generated_links` (`url`)")
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE `favorite_deals` (`deal_id` TEXT NOT NULL, `userId` TEXT NOT NULL, `addedAt` INTEGER, PRIMARY KEY(`deal_id`, `userId`))")
                db.execSQL("CREATE TABLE `generated_links_new` (`url` TEXT NOT NULL, `userId` TEXT NOT NULL, `createdAt` INTEGER, PRIMARY KEY(`url`, `userId`))")
                db.execSQL("DROP TABLE `generated_links`")
                db.execSQL("ALTER TABLE `generated_links_new` RENAME TO `generated_links`")
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `supported_stores` (`name` TEXT NOT NULL, `logoUrl` TEXT NOT NULL, PRIMARY KEY(`name`))")
            }
        }

        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `user_profile` (`uid` TEXT NOT NULL, `name` TEXT, `email` TEXT, `phone` TEXT, `profilePic` TEXT, PRIMARY KEY(`uid`))")
            }
        }

        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `notified_deals` (`deal_id` TEXT NOT NULL, `isSeen` INTEGER NOT NULL, `timestamp` INTEGER NOT NULL, `deal_deal_id` TEXT NOT NULL, `deal_discount` TEXT, `deal_image` TEXT, `deal_originalPrice` TEXT, `deal_posted_on` TEXT, `deal_price` TEXT, `deal_redirectUrl` TEXT, `deal_store` TEXT, `deal_title` TEXT, `deal_url` TEXT, PRIMARY KEY(`deal_id`))")
            }
        }

        /**
         * Gets the singleton instance of the [AppDatabase].
         *
         * @param context The application context.
         * @return The singleton instance of the [AppDatabase].
         */
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "offnbuy_database"
                )
                    .addMigrations(
                        MIGRATION_1_2,
                        MIGRATION_2_3,
                        MIGRATION_3_4,
                        MIGRATION_4_5,
                        MIGRATION_5_6,
                        MIGRATION_6_7
                    )
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

/**
 * Type converters for the Room database.
 */
class Converters {
    /**
     * Converts a timestamp to a [Date] object.
     *
     * @param value The timestamp.
     * @return The [Date] object.
     */
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    /**
     * Converts a [Date] object to a timestamp.
     *
     * @param date The [Date] object.
     * @return The timestamp.
     */
    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}