package org.personal.videotogether.model.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user")
data class UserCacheEntity (
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "id")
    val id : Int,

    @ColumnInfo(name = "email")
    val email : String,

    @ColumnInfo(name = "password")
    val password : String,

    @ColumnInfo(name = "name")
    val name : String?,

    @ColumnInfo(name = "profile_image_url")
    val profile_image_url : String?
)