package org.personal.videotogether.room

import androidx.room.*
import org.personal.videotogether.room.entity.UserCacheEntity

@Dao
interface UserDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserData(userCacheEntity: UserCacheEntity) : Long

    @Query("SELECT * FROM user")
    suspend fun getUserData() : List<UserCacheEntity>

    @Query("DELETE FROM user")
    suspend fun deleteAllUserData() : Int

    @Update
    suspend fun updateUserData(vararg userCacheEntity: UserCacheEntity)
}