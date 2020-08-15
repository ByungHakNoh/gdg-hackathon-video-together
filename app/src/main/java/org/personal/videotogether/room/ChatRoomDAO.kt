package org.personal.videotogether.room

import androidx.room.*
import org.personal.videotogether.room.entity.ChatRoomCacheEntity

@Dao
interface ChatRoomDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatRoom(chatRoomCacheEntity: ChatRoomCacheEntity) : Long

    @Query("SELECT * FROM chat_room")
    suspend fun getChatRooms() : List<ChatRoomCacheEntity>

    @Query("DELETE FROM chat_room")
    suspend fun deleteAllChatRoomData() : Int

    @Update
    suspend fun updateChatRoomData(vararg chatRoomCacheEntity: ChatRoomCacheEntity)
}