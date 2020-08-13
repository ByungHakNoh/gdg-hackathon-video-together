package org.personal.videotogether.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.scopes.ActivityScoped
import org.personal.videotogether.util.view.DataStateHandler
import org.personal.videotogether.util.view.ImageHandler
import org.personal.videotogether.util.view.ViewHandler
import org.personal.videotogether.view.dialog.LoadingDialog

@InstallIn(ActivityComponent::class)
@Module
object FragmentModule {
    @ActivityScoped
    @Provides
    fun provideLoadingDialog() : LoadingDialog {
        return LoadingDialog()
    }

    @ActivityScoped
    @Provides
    fun provideHandleImage() : ImageHandler {
        return ImageHandler()
    }

    @ActivityScoped
    @Provides
    fun provideDataStateHandler(loadingDialog: LoadingDialog): DataStateHandler {
        return DataStateHandler(loadingDialog)
    }

    @ActivityScoped
    @Provides
    fun provideViewHandler(): ViewHandler {
        return ViewHandler()
    }
}