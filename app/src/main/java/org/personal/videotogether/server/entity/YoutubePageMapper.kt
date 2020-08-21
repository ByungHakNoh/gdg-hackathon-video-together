package org.personal.videotogether.server.entity

import org.personal.videotogether.domianmodel.YoutubePageData
import org.personal.videotogether.util.EntityMapper
import javax.inject.Inject

class YoutubePageMapper
@Inject
constructor(
    private val youtubeMapper: YoutubeMapper
): EntityMapper<YoutubePageEntity, YoutubePageData> {

    override fun mapFromEntity(entity: YoutubePageEntity): YoutubePageData {
        return YoutubePageData(
            nextPageUrl = entity.nextPageUrl,
            nextPageToken = entity.nextPageToken,
            youtubeDataList = youtubeMapper.mapFromEntityList(entity.youtubeDataList)
        )
    }

    override fun mapToEntity(domainModel: YoutubePageData): YoutubePageEntity {
        return return YoutubePageEntity(
            nextPageUrl = domainModel.nextPageUrl,
            nextPageToken = domainModel.nextPageToken,
            youtubeDataList = youtubeMapper.mapToEntityList(domainModel.youtubeDataList)
        )
    }
}