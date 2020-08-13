package org.personal.videotogether.model.local

import androidx.room.Database
import androidx.room.RoomDatabase
import org.personal.videotogether.model.local.entity.FriendCacheEntity
import org.personal.videotogether.model.local.entity.UserCacheEntity

@Database(
    entities = [
        UserCacheEntity::class,
        FriendCacheEntity::class
    ], version = 3
)
abstract class VideoTogetherDatabase : RoomDatabase() {
    abstract fun userDAO(): UserDAO
    abstract fun friendDAO(): FriendDAO

    companion object {
        val DATABASE_NAME: String = "video_together"
    }
}