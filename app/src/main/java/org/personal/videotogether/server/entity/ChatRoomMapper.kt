package org.personal.videotogether.server.entity

import dagger.hilt.android.scopes.ActivityRetainedScoped
import org.personal.videotogether.domianmodel.ChatRoomData
import org.personal.videotogether.util.EntityMapper
import javax.inject.Inject

class ChatRoomMapper
@ActivityRetainedScoped
@Inject
constructor() : EntityMapper<ChatRoomEntity, ChatRoomData> {
    override fun mapFromEntity(entity: ChatRoomEntity): ChatRoomData {
        return ChatRoomData(
            id = entity.id,
            lastChatMessage = entity.lastChatMessage,
            participantList = entity.participantList
        )
    }

    override fun mapToEntity(domainModel: ChatRoomData): ChatRoomEntity {
        return ChatRoomEntity(
            id = domainModel.id,
            lastChatMessage = domainModel.lastChatMessage,
            participantList = domainModel.participantList
        )
    }

    fun mapFromEntityList (entityList:List<ChatRoomEntity>) : List<ChatRoomData> {
        return entityList.map { chatRoomEntity ->
            mapFromEntity(chatRoomEntity)
        }
    }
}