package org.personal.videotogether.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "friend")
data class FriendCacheEntity(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "id")
    val id : Int,

    @ColumnInfo(name = "email")
    val email : String,

    @ColumnInfo(name = "name")
    val name : String?,

    @ColumnInfo(name = "profile_image_url")
    val profile_image_url : String?
)