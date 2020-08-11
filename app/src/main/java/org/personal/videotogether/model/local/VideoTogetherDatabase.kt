package org.personal.videotogether.model.local

import androidx.room.Database
import androidx.room.RoomDatabase
import org.personal.videotogether.model.local.UserDAO
import org.personal.videotogether.model.local.entity.UserCacheEntity

@Database(entities = [ UserCacheEntity::class], version = 1)
abstract class VideoTogetherDatabase : RoomDatabase() {
    abstract fun userDAO() : UserDAO

    companion object {
        val DATABASE_NAME: String = "video_together"
    }
}