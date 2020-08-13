package org.personal.videotogether.server.entity

import org.personal.videotogether.domianmodel.ChatRoomData
import org.personal.videotogether.util.EntityMapper
import javax.inject.Inject

class ChatRoomMapper
@Inject
constructor() : EntityMapper<ChatRoomEntity, ChatRoomData> {
    override fun mapFromEntity(entity: ChatRoomEntity): ChatRoomData {
        return ChatRoomData(
            id = entity.id,
            lastChatMessage = entity.lastChatMessage,
            participantsList = entity.participantsList
        )
    }

    override fun mapToEntity(domainModel: ChatRoomData): ChatRoomEntity {
        return ChatRoomEntity(
            id = domainModel.id,
            lastChatMessage = domainModel.lastChatMessage,
            participantsList = domainModel.participantsList
        )
    }

    fun mapFromEntityList (entityList:List<ChatRoomEntity>) : List<ChatRoomData> {
        return entityList.map { chatRoomEntity ->
            mapFromEntity(chatRoomEntity)
        }
    }
}