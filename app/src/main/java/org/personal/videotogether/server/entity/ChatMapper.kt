package org.personal.videotogether.server.entity

import org.personal.videotogether.domianmodel.ChatData
import org.personal.videotogether.util.EntityMapper
import javax.inject.Inject

class ChatMapper
@Inject
constructor(): EntityMapper<ChatEntity, ChatData> {

    override fun mapFromEntity(entity: ChatEntity): ChatData {
        return ChatData(
            roomId = entity.roomId,
            senderId = entity.senderId,
            senderName = entity.senderName,
            profileImageUrl = entity.profileImageUrl,
            message = entity.message,
            messageTime = entity.messageTime
        )
    }

    override fun mapToEntity(domainModel: ChatData): ChatEntity {
        return ChatEntity(
            roomId = domainModel.roomId,
            senderId = domainModel.senderId,
            senderName = domainModel.senderName,
            profileImageUrl = domainModel.profileImageUrl,
            message = domainModel.message,
            messageTime = domainModel.messageTime!!
        )
    }

    fun mapFromEntityList(chatEntityList: List<ChatEntity>): List<ChatData>{
        return chatEntityList.map { chatEntity ->
            mapFromEntity(chatEntity)
        }
    }

    fun mapToEntityList(chatDataList: List<ChatData>): List<ChatEntity>{
        return chatDataList.map { chatData ->
            mapToEntity(chatData)
        }
    }
}