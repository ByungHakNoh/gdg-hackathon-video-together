package org.personal.videotogether.server.entity

import org.personal.videotogether.domianmodel.UserData
import org.personal.videotogether.util.EntityMapper
import javax.inject.Inject

class UserMapper
@Inject
constructor() : EntityMapper<UserEntity, UserData> {

    override fun mapFromEntity(entity: UserEntity): UserData {
        return UserData(
            id = entity.id,
            email = entity.email,
            password = entity.password,
            name = entity.name,
            profileImageUrl = entity.profile_image_url
        )
    }

    override fun mapToEntity(domainModel: UserData): UserEntity {
        return UserEntity(
            id = domainModel.id,
            email = domainModel.email,
            password = domainModel.password,
            name = domainModel.name,
            profile_image_url = domainModel.profileImageUrl
        )
    }

    fun mapFromEntityList(entityList: List<UserEntity>): List<UserData> {
        return entityList.map { userEntity ->
            mapFromEntity(userEntity)
        }
    }

    fun mapToEntityList(entityList: List<UserData>): List<UserEntity> {
        return entityList.map { userData ->
            mapToEntity(userData)
        }
    }
}