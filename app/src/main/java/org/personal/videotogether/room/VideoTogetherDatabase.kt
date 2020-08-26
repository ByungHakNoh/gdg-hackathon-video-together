package org.personal.videotogether.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import org.personal.videotogether.room.entity.ChatCacheEntity
import org.personal.videotogether.room.entity.ChatRoomCacheEntity
import org.personal.videotogether.room.entity.FriendCacheEntity
import org.personal.videotogether.room.entity.UserCacheEntity

@Database(
    entities = [
        UserCacheEntity::class,
        FriendCacheEntity::class,
        ChatRoomCacheEntity::class,
        ChatCacheEntity::class
    ],
    version = 8,
    exportSchema = false
)
abstract class VideoTogetherDatabase : RoomDatabase() {
    abstract fun userDAO(): UserDAO
    abstract fun friendDAO(): FriendDAO
    abstract fun chatRoomDAO(): ChatRoomDAO
    abstract fun chatDAO(): ChatDAO

    companion object {
        const val DATABASE_NAME: String = "video_together"
    }
}