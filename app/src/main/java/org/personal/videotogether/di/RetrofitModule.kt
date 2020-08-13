package org.personal.videotogether.di

import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.personal.videotogether.server.RetrofitRequest
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

@Module
@InstallIn(ActivityRetainedComponent::class)
object RetrofitModule {

    // ------------------ Gson 객체 ------------------
    @ActivityRetainedScoped
    @Provides
    fun provideGsonBuilder(): Gson {
        return GsonBuilder()
            .setLenient()
            .excludeFieldsWithoutExposeAnnotation()
            .create() // TODO : @expose 어떤 역할인지 찾아보기
    }


    // ------------------ Logging 을 위한 OkHttp 객체(Interceptor 탑재) ------------------
    @ActivityRetainedScoped
    @Provides
    fun provideHttpLogging(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor(HttpLoggingInterceptor.Logger { message ->
            Log.e("http logger", "provideHttpLogging: $message")
        }).setLevel(HttpLoggingInterceptor.Level.BODY)
    }

    @ActivityRetainedScoped
    @Provides
    fun provideOkHttpClient(httpLoggingInterceptor: HttpLoggingInterceptor): OkHttpClient {
        return OkHttpClient.Builder()
            .addNetworkInterceptor(httpLoggingInterceptor)
            .build()
    }

    // ------------------ Retrofit2 객체 ------------------
    @ActivityRetainedScoped
    @Provides
    fun provideRetrofit(gson: Gson, okHttpClient: OkHttpClient): Retrofit.Builder {
        return Retrofit.Builder()
            .baseUrl("https://couple-space.tk/")
            .client(okHttpClient)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(gson))
    }

    @ActivityRetainedScoped
    @Provides
    fun provideMainService(retrofit: Retrofit.Builder): RetrofitRequest {
        return retrofit
            .build()
            .create(RetrofitRequest::class.java)
    }
}