package org.personal.videotogether.room.entity

import org.personal.videotogether.domianmodel.FriendData
import org.personal.videotogether.util.EntityMapper
import javax.inject.Inject

class FriendCacheMapper
@Inject
constructor() : EntityMapper<FriendCacheEntity, FriendData> {
    override fun mapFromEntity(entity: FriendCacheEntity): FriendData {
        return FriendData(
            id = entity.id,
            email = entity.email,
            name = entity.name,
            profileImageUrl = entity.profile_image_url,
            isSelected = null
        )
    }

    override fun mapToEntity(domainModel: FriendData): FriendCacheEntity {
       return FriendCacheEntity(
           id = domainModel.id,
           email = domainModel.email,
           name = domainModel.name,
           profile_image_url = domainModel.profileImageUrl
       )
    }

    fun mapToEntityList(domainModelList: List<FriendData>) :List<FriendCacheEntity> {
        return domainModelList.map {domainModel ->
            mapToEntity(domainModel)
        }
    }

    fun mapFromEntityList(entityList: List<FriendCacheEntity>) : List<FriendData> {
        return entityList.map {friendCacheEntity ->
            mapFromEntity(friendCacheEntity)
        }
    }
}