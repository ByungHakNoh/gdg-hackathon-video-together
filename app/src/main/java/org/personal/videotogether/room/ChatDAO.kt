package org.personal.videotogether.room

import androidx.room.*
import org.personal.videotogether.room.entity.ChatCacheEntity

@Dao
interface ChatDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChat(chatCacheEntity: ChatCacheEntity) : Long

    @Query("SELECT * FROM chat WHERE room_id = :roomId")
    suspend fun getChatList(roomId: Int) : List<ChatCacheEntity>

    @Query("DELETE FROM chat")
    suspend fun deleteAllChat() : Int

    @Update
    suspend fun updateChat(vararg chatCacheEntity: ChatCacheEntity)
}