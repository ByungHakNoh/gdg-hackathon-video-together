package org.personal.videotogether.room.entity

import org.personal.videotogether.domianmodel.ChatData
import org.personal.videotogether.util.EntityMapper
import javax.inject.Inject

class ChatCacheMapper
@Inject
constructor() : EntityMapper<ChatCacheEntity, ChatData> {
    override fun mapFromEntity(entity: ChatCacheEntity): ChatData {
        return ChatData(
            roomId = entity.roomId,
            senderId = entity.senderId,
            senderName = entity.senderName,
            profileImageUrl = entity.profileImageUrl,
            message = entity.message,
            messageTime = entity.messageTime
        )
    }

    override fun mapToEntity(domainModel: ChatData): ChatCacheEntity {
        return ChatCacheEntity(
            id = null,
            roomId = domainModel.roomId,
            senderId = domainModel.senderId,
            senderName = domainModel.senderName,
            profileImageUrl = domainModel.profileImageUrl,
            message = domainModel.message,
            messageTime = domainModel.messageTime!!
        )
    }

    fun mapFromEntityList(chatCacheEntityList: List<ChatCacheEntity>): List<ChatData> {
        return chatCacheEntityList.map { chatCacheEntity ->
            mapFromEntity(chatCacheEntity)
        }
    }

    fun mapToEntityList(chatDataList: List<ChatData>): List<ChatCacheEntity> {
        return chatDataList.map { chatData ->
            mapToEntity(chatData)
        }
    }
}