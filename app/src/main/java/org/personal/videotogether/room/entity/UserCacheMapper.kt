package org.personal.videotogether.room.entity

import org.personal.videotogether.domianmodel.UserData
import org.personal.videotogether.util.EntityMapper
import javax.inject.Inject

class UserCacheMapper
@Inject
constructor() : EntityMapper<UserCacheEntity, UserData> {
    override fun mapFromEntity(entity: UserCacheEntity): UserData {
        return UserData(
            id = entity.id,
            email = entity.email,
            password = entity.password,
            name = entity.name,
            profileImageUrl = entity.profile_image_url
        )
    }

    override fun mapToEntity(domainModel: UserData): UserCacheEntity {
        return UserCacheEntity(
            id = domainModel.id,
            email = domainModel.email,
            password = domainModel.password,
            name = domainModel.name,
            profile_image_url = domainModel.profileImageUrl
        )
    }
}