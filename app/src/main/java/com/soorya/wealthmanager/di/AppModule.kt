package com.soorya.wealthmanager.di

import android.content.Context
import androidx.room.Room
import com.soorya.wealthmanager.data.local.WealthDatabase
import com.soorya.wealthmanager.data.local.entity.TransactionDao
import com.soorya.wealthmanager.data.remote.NotionApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides @Singleton
    fun provideDb(@ApplicationContext ctx: Context): WealthDatabase =
        Room.databaseBuilder(ctx, WealthDatabase::class.java, WealthDatabase.NAME)
            .fallbackToDestructiveMigration().build()

    @Provides fun provideDao(db: WealthDatabase): TransactionDao = db.transactionDao()

    @Provides @Singleton
    fun provideOkHttp(): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            chain.proceed(chain.request().newBuilder()
                .addHeader("Notion-Version", NotionApi.VERSION)
                .addHeader("Content-Type", "application/json")
                .build())
        }
        .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    @Provides @Singleton
    fun provideNotionApi(client: OkHttpClient): NotionApi =
        Retrofit.Builder().baseUrl(NotionApi.BASE).client(client)
            .addConverterFactory(GsonConverterFactory.create()).build()
            .create(NotionApi::class.java)
}
