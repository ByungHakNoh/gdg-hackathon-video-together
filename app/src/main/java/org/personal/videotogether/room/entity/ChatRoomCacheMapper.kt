package org.personal.videotogether.room.entity

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.personal.videotogether.domianmodel.ChatRoomData
import org.personal.videotogether.util.EntityMapper
import javax.inject.Inject

class ChatRoomCacheMapper
@Inject
constructor(
    private val userCacheMapper: UserCacheMapper
) : EntityMapper<ChatRoomCacheEntity, ChatRoomData> {

    private val gson by lazy { Gson() }

    override fun mapFromEntity(entity: ChatRoomCacheEntity): ChatRoomData {
        val userCacheEntityList = gson.fromJson<List<UserCacheEntity>>(entity.participant_list, object :TypeToken<List<UserCacheEntity>>() {}.type)
        val participantList = userCacheMapper.mapFromEntityList(userCacheEntityList)

        return ChatRoomData(
            id = entity.id,
            lastChatMessage = entity.last_chat_message,
            lastChatTime = entity.last_message_time,
            unReadChatCount = entity.un_read_chat_count,
            participantList = participantList,
            isSelected = false
        )
    }

    override fun mapToEntity(domainModel: ChatRoomData): ChatRoomCacheEntity {
        val userCacheEntityList= userCacheMapper.mapToEntityList(domainModel.participantList)
        val participantListJson = gson.toJson(userCacheEntityList)

        return ChatRoomCacheEntity(
            id = domainModel.id,
            last_chat_message = domainModel.lastChatMessage,
            last_message_time = domainModel.lastChatTime,
            un_read_chat_count = domainModel.unReadChatCount,
            participant_list = participantListJson
        )
    }

    fun mapFromEntityList(entityList: List<ChatRoomCacheEntity>) : List<ChatRoomData> {
        return entityList.map {chatRoomCacheEntity ->
            mapFromEntity(chatRoomCacheEntity)
        }
    }

    fun mapToEntityList(domainModelList: List<ChatRoomData>) : List<ChatRoomCacheEntity> {
        return domainModelList.map {chatRoomData ->
            mapToEntity(chatRoomData)
        }
    }
}