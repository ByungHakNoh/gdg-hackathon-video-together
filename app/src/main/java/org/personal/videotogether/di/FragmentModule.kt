package org.personal.videotogether.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import org.personal.videotogether.util.DataStateHandler
import org.personal.videotogether.util.ImageHandler
import org.personal.videotogether.view.dialog.LoadingDialog
import javax.inject.Singleton

@InstallIn(ApplicationComponent::class)
@Module
object FragmentModule {
    @Singleton
    @Provides
    fun provideLoadingDialog() : LoadingDialog {
        return LoadingDialog()
    }

    @Singleton
    @Provides
    fun provideHandleImage() : ImageHandler {
        return ImageHandler()
    }

    @Singleton
    @Provides
    fun provideDataStateHandler(loadingDialog: LoadingDialog): DataStateHandler {
        return DataStateHandler(loadingDialog)
    }
}