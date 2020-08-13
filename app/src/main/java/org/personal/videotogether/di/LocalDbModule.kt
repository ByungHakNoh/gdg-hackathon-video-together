package org.personal.videotogether.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import org.personal.videotogether.room.FriendDAO
import org.personal.videotogether.room.UserDAO
import org.personal.videotogether.room.VideoTogetherDatabase
import org.personal.videotogether.util.SharedPreferenceHelper
import javax.inject.Singleton

@InstallIn(ApplicationComponent::class)
@Module
object LocalDbModule {

    @Singleton
    @Provides
    fun providePreferenceHelper() : SharedPreferenceHelper {
        return SharedPreferenceHelper()
    }

    @Singleton
    @Provides
    fun provideDatabase(@ApplicationContext context: Context): VideoTogetherDatabase {
        return Room.databaseBuilder(
            context,
            VideoTogetherDatabase::class.java,
            VideoTogetherDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Singleton
    @Provides
    fun provideUserDAO(videoTogetherDatabase: VideoTogetherDatabase) : UserDAO {
        return videoTogetherDatabase.userDAO()
    }

    @Singleton
    @Provides
    fun provideFriendDAO(videoTogetherDatabase: VideoTogetherDatabase) : FriendDAO {
        return videoTogetherDatabase.friendDAO()
    }
}