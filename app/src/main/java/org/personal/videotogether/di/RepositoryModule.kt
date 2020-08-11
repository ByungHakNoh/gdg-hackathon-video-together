package org.personal.videotogether.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import org.personal.videotogether.model.server.RetrofitRequest
import org.personal.videotogether.model.local.entity.UserCacheMapper
import org.personal.videotogether.model.local.UserDAO
import org.personal.videotogether.model.repository.UserRepository
import org.personal.videotogether.model.server.entity.UserDataMapper
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
        userDataMapper: UserDataMapper
    ): UserRepository {
        return UserRepository(retrofitRequest, userDAO, userCacheMapper, userDataMapper)
    }
}