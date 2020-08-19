package org.personal.videotogether.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped
import org.personal.videotogether.repository.*
import org.personal.videotogether.room.ChatDAO
import org.personal.videotogether.room.FriendDAO
import org.personal.videotogether.server.RetrofitRequest
import org.personal.videotogether.room.entity.UserCacheMapper
import org.personal.videotogether.room.UserDAO
import org.personal.videotogether.room.entity.FriendCacheMapper
import org.personal.videotogether.room.ChatRoomDAO
import org.personal.videotogether.room.entity.ChatCacheMapper
import org.personal.videotogether.room.entity.ChatRoomCacheMapper
import org.personal.videotogether.server.entity.*
import java.net.Socket

@InstallIn(ActivityRetainedComponent::class)
@Module
object RepositoryModule {

    @ActivityRetainedScoped
    @Provides
    fun provideUserRepository(
        retrofitRequest: RetrofitRequest,
        userDAO: UserDAO,
        userCacheMapper: UserCacheMapper,
        userMapper: UserMapper
    ): UserRepository {
        return UserRepository(retrofitRequest, userDAO, userCacheMapper, userMapper)
    }

    @ActivityRetainedScoped
    @Provides
    fun provideFriendRepository(
        retrofitRequest: RetrofitRequest,
        friendDAO: FriendDAO,
        friendCacheMapper: FriendCacheMapper,
        friendMapper: FriendMapper
    ): FriendRepository {
        return FriendRepository(retrofitRequest, friendDAO, friendCacheMapper, friendMapper)
    }

    @ActivityRetainedScoped
    @Provides
    fun provideYoutubeRepository(
        retrofitRequest: RetrofitRequest,
        youtubeMapper: YoutubeMapper
    ): YoutubeRepository {
        return YoutubeRepository(retrofitRequest, youtubeMapper)
    }

    @ActivityRetainedScoped
    @Provides
    fun provideChatRepository(
        retrofitRequest: RetrofitRequest,
        chatRoomDAO: ChatRoomDAO,
        chatDAO: ChatDAO,
        chatRoomMapper: ChatRoomMapper,
        chatRoomCacheMapper: ChatRoomCacheMapper,
        chatMapper: ChatMapper,
        chatCacheMapper: ChatCacheMapper,
        userMapper: UserMapper,
        friendMapper: FriendMapper
    ): ChatRepository {
        return ChatRepository(
            retrofitRequest,
            chatRoomDAO,
            chatDAO,
            chatRoomMapper,
            chatRoomCacheMapper,
            chatMapper,
            chatCacheMapper,
            userMapper,
            friendMapper
        )
    }

    @ActivityRetainedScoped
    @Provides
    fun provideSocketRepository(
        retrofitRequest: RetrofitRequest
    ): SocketRepository {
        return SocketRepository(retrofitRequest)
    }
}