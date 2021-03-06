package knf.kuma.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import knf.kuma.database.dao.*
import knf.kuma.pojos.*

@Database(entities = [RecentObject::class, AnimeObject::class, FavoriteObject::class, AnimeObject.WebInfo.AnimeChapter::class, NotificationObj::class, DownloadObject::class, RecordObject::class, SeeingObject::class, ExplorerObject::class, GenreStatusObject::class, QueueObject::class],
        version = 11)
abstract class CacheDB : RoomDatabase() {

    abstract fun recentsDAO(): RecentsDAO

    abstract fun animeDAO(): AnimeDAO

    abstract fun favsDAO(): FavsDAO

    abstract fun chaptersDAO(): ChaptersDAO

    abstract fun notificationDAO(): NotificationDAO

    abstract fun downloadsDAO(): DownloadsDAO

    abstract fun recordsDAO(): RecordsDAO

    abstract fun seeingDAO(): SeeingDAO

    abstract fun explorerDAO(): ExplorerDAO

    abstract fun queueDAO(): QueueDAO

    abstract fun genresDAO(): GenresDAO

    companion object {
        private val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE `genrestatusobject` (`key` INTEGER NOT NULL, " + "`name` TEXT, `count` INTEGER NOT NULL, PRIMARY KEY(`key`))")
            }
        }
        private val MIGRATION_2_3: Migration = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE `queueobject` (`key` INTEGER, `id` INTEGER NOT NULL," + "`number` TEXT, `eid` TEXT,`isFile` INTEGER NOT NULL,`link` TEXT,`name` TEXT,`aid` TEXT,`time` INTEGER NOT NULL, PRIMARY KEY (`id`))")
            }
        }
        private val MIGRATION_3_4: Migration = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE `queueobject`  ADD COLUMN `uri` TEXT")
            }
        }
        private val MIGRATION_4_5: Migration = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE `explorerobject`  ADD COLUMN `aid` TEXT")
            }
        }
        private val MIGRATION_5_6: Migration = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE `downloadobject`  ADD COLUMN `headers` TEXT")
            }
        }
        private val MIGRATION_6_7: Migration = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE `downloadobject`  ADD COLUMN `did` TEXT")
                database.execSQL("ALTER TABLE `downloadobject`  ADD COLUMN `eta` TEXT")
                database.execSQL("ALTER TABLE `downloadobject`  ADD COLUMN `speed` TEXT")
            }
        }
        private val MIGRATION_7_8: Migration = object : Migration(7, 8) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE `downloadobject`  ADD COLUMN `time` INTEGER NOT NULL DEFAULT 0")
            }
        }
        private val MIGRATION_8_9: Migration = object : Migration(8, 9) {
            override fun migrate(database: SupportSQLiteDatabase) {

            }
        }
        private val MIGRATION_9_10: Migration = object : Migration(9, 10) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE IF NOT EXISTS `recentobject_tmp` (`key` INTEGER NOT NULL, `aid` TEXT, `eid` TEXT, `name` TEXT, `chapter` TEXT, `url` TEXT, `img` TEXT, PRIMARY KEY(`key`))")
                database.execSQL("DROP TABLE `recentobject`")
                database.execSQL("ALTER TABLE `recentobject_tmp` RENAME TO `recentobject`")
            }
        }

        private val MIGRATION_10_11: Migration = object : Migration(10, 11) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE `seeingobject`  ADD COLUMN `state` INTEGER NOT NULL DEFAULT 1")
            }
        }
        lateinit var INSTANCE: CacheDB

        fun init(context: Context) {
            INSTANCE = Room.databaseBuilder(context, CacheDB::class.java, "cache-db")
                    .allowMainThreadQueries()
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10, MIGRATION_10_11).build()
        }

        fun createAndGet(context: Context): CacheDB {
            return Room.databaseBuilder(context, CacheDB::class.java, "cache-db")
                    .allowMainThreadQueries()
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10, MIGRATION_10_11).build()
        }
    }

}
