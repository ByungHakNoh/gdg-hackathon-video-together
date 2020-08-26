package org.personal.videotogether.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.components.ApplicationComponent
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
import javax.inject.Singleton

@InstallIn(ApplicationComponent::class)
@Module
object RepositoryModule {

    @Singleton
    @Provides
    fun provideUserRepository(
        retrofitRequest: RetrofitRequest,
        userDAO: UserDAO,
        userCacheMapper: UserCacheMapper,
        userMapper: UserMapper,
        chatRoomDAO: ChatRoomDAO,
        friendDAO: FriendDAO
    ): UserRepository {
        return UserRepository(retrofitRequest, userDAO, userCacheMapper, userMapper, chatRoomDAO, friendDAO)
    }

    @Singleton
    @Provides
    fun provideFriendRepository(
        retrofitRequest: RetrofitRequest,
        friendDAO: FriendDAO,
        friendCacheMapper: FriendCacheMapper,
        friendMapper: FriendMapper
    ): FriendRepository {
        return FriendRepository(retrofitRequest, friendDAO, friendCacheMapper, friendMapper)
    }

    @Singleton
    @Provides
    fun provideYoutubeRepository(
        retrofitRequest: RetrofitRequest,
        youtubeMapper: YoutubeMapper,
        youtubePageMapper: YoutubePageMapper
    ): YoutubeRepository {
        return YoutubeRepository(retrofitRequest, youtubeMapper, youtubePageMapper)
    }

    @Singleton
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

    @Singleton
    @Provides
    fun provideSocketRepository(): SocketRepository {
        return SocketRepository()
    }
}