package org.personal.videotogether.server.entity

import org.personal.videotogether.domianmodel.FriendData
import org.personal.videotogether.util.EntityMapper
import javax.inject.Inject

class FriendMapper
@Inject
constructor() : EntityMapper<FriendEntity, FriendData>  {
    override fun mapFromEntity(entity: FriendEntity): FriendData {
        return FriendData(
            id = entity.id,
            email = entity.email,
            name = entity.name,
            profileImageUrl = entity.profile_image_url,
            isSelected = null
        )
    }

    override fun mapToEntity(domainModel: FriendData): FriendEntity {
        return FriendEntity(
            id = domainModel.id,
            email = domainModel.email,
            name = domainModel.name,
            profile_image_url = domainModel.profileImageUrl
        )
    }

    fun mapUserDataToFriendData(userEntity: UserEntity) : FriendData {
        return FriendData(
            id = userEntity.id,
            email = userEntity.email,
            name = userEntity.name,
            profileImageUrl = userEntity.profile_image_url,
            isSelected = null
        )
    }

    fun mapToEntityList(domainModelList: List<FriendData>) : List<FriendEntity> {
        return domainModelList.map {friendData ->
            mapToEntity(friendData)
        }
    }

    fun mapFromEntityList(entityList: List<FriendEntity>) : List<FriendData> {
        return entityList.map {friendEntity ->
            mapFromEntity(friendEntity)
        }
    }
}