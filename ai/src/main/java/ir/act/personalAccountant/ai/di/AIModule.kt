package ir.act.personalAccountant.ai.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ir.act.personalAccountant.ai.data.remote.OpenAIApiService
import ir.act.personalAccountant.ai.data.remote.OpenAIClient
import ir.act.personalAccountant.ai.data.repository.AIRepository
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AIModule {

    @Provides
    @Singleton
    fun provideGson(): Gson {
        return GsonBuilder()
            .setLenient()
            .create()
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(90, TimeUnit.SECONDS)      // Time to establish connection
            .readTimeout(300, TimeUnit.SECONDS)        // Time to read response (5 minutes)
            .writeTimeout(90, TimeUnit.SECONDS)        // Time to write request
            .callTimeout(360, TimeUnit.SECONDS)        // Total call timeout (6 minutes)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, gson: Gson): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.openai.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Provides
    @Singleton
    fun provideOpenAIApiService(retrofit: Retrofit): OpenAIApiService {
        return retrofit.create(OpenAIApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideOpenAIClient(apiService: OpenAIApiService, gson: Gson): OpenAIClient {
        return OpenAIClient(apiService, gson)
    }

    @Provides
    @Singleton
    fun provideAIRepository(dataStore: DataStore<Preferences>): AIRepository {
        return AIRepository(dataStore)
    }
}