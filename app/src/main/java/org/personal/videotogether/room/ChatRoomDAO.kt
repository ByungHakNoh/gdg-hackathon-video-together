package org.personal.videotogether.room

import androidx.room.*
import org.personal.videotogether.room.entity.ChatRoomCacheEntity

@Dao
interface ChatRoomDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatRoom(chatRoomCacheEntity: ChatRoomCacheEntity): Long

    @Query("SELECT * FROM chat_room ORDER BY id DESC")
    suspend fun getChatRooms(): List<ChatRoomCacheEntity>

    @Query("DELETE FROM chat_room")
    suspend fun deleteAllChatRoomData(): Int

    @Query("UPDATE chat_room SET last_chat_message = :lastChatMessage, last_message_time = :lastChatTime WHERE id = :roomId")
    suspend fun updateLastChat(roomId: Int, lastChatMessage: String, lastChatTime: String): Int

    @Query("UPDATE chat_room SET un_read_chat_count = un_read_chat_count+1 WHERE id = :roomId")
    suspend fun updateUnRead(roomId: Int): Int

    @Update
    suspend fun updateChatRoomData(vararg chatRoomCacheEntity: ChatRoomCacheEntity)
}