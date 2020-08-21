package org.personal.videotogether.server.entity

import org.personal.videotogether.domianmodel.YoutubeData
import org.personal.videotogether.util.EntityMapper
import javax.inject.Inject

class YoutubeMapper
@Inject
constructor() : EntityMapper<YoutubeEntity, YoutubeData> {
    override fun mapFromEntity(entity: YoutubeEntity): YoutubeData {
        return YoutubeData(
            channelTitle = entity.channelTitle,
            channelThumbnail = entity.channelThumbnail,
            title = entity.title,
            videoId = entity.videoId
        )
    }

    override fun mapToEntity(domainModel: YoutubeData): YoutubeEntity {
        return YoutubeEntity(
            channelTitle = domainModel.channelTitle,
            channelThumbnail = domainModel.channelThumbnail,
            title = domainModel.title,
            videoId = domainModel.videoId
        )
    }

    fun mapFromEntityList(entityList:List<YoutubeEntity>): List<YoutubeData> {
        return entityList.map { youtubeEntity ->
            mapFromEntity(youtubeEntity)
        }
    }

    fun mapToEntityList(domainModelList:List<YoutubeData>): List<YoutubeEntity> {
        return domainModelList.map { youtubeData ->
            mapToEntity(youtubeData)
        }
    }
}