package knf.kuma.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import knf.kuma.database.BaseConverter
import knf.kuma.pojos.AnimeObject

@Dao
@TypeConverters(BaseConverter::class)
interface ChaptersDAO {

    @get:Query("SELECT * FROM animechapter")
    val all: MutableList<AnimeObject.WebInfo.AnimeChapter>

    @Query("SELECT count(*) FROM animechapter")
    fun init(): Int

    @Query("SELECT * FROM animechapter WHERE eid = :eid LIMIT 1")
    fun chapterSeen(eid: String): LiveData<AnimeObject.WebInfo.AnimeChapter>

    @Query("SELECT count(*) FROM animechapter WHERE eid = :eid")
    fun chapterIsSeen(eid: String): Boolean

    @Query("SELECT * FROM animechapter WHERE eid IN (:eids) ORDER BY eid DESC LIMIT 1")
    fun getLast(eids: MutableList<String>): AnimeObject.WebInfo.AnimeChapter?

    @Query("SELECT * FROM animechapter WHERE aid LIKE :aid ORDER BY `key` DESC LIMIT 1")
    fun getLastByAid(aid: String): AnimeObject.WebInfo.AnimeChapter?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addChapter(chapter: AnimeObject.WebInfo.AnimeChapter)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addAll(list: MutableList<AnimeObject.WebInfo.AnimeChapter>)

    @Delete
    fun deleteChapter(chapter: AnimeObject.WebInfo.AnimeChapter)

    @Query("DELETE FROM animechapter")
    fun clear()
}
