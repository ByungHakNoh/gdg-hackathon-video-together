package org.personal.videotogether.room

import androidx.room.Database
import androidx.room.RoomDatabase
import org.personal.videotogether.room.entity.FriendCacheEntity
import org.personal.videotogether.room.entity.UserCacheEntity

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
        const val DATABASE_NAME: String = "video_together"
    }
}