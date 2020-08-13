package org.personal.videotogether.room

import androidx.room.*
import org.personal.videotogether.room.entity.FriendCacheEntity
@Dao
interface FriendDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFriendsData(friendCacheEntity: FriendCacheEntity) : Long

    @Query("SELECT * FROM friend")
    suspend fun getFriendList() : List<FriendCacheEntity>

    @Query("DELETE FROM friend")
    suspend fun deleteAllFriendsData() : Int

    @Update
    suspend fun updateFriendData(vararg friendCacheEntity: FriendCacheEntity)
}