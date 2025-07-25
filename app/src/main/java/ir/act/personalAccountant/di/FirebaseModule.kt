package ir.act.personalAccountant.di

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ir.act.personalAccountant.data.sync.FirebaseSyncStrategy
import ir.act.personalAccountant.domain.sync.CloudSyncStrategy
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class FirebaseModule {

    @Binds
    @Singleton
    abstract fun bindCloudSyncStrategy(
        firebaseSyncStrategy: FirebaseSyncStrategy
    ): CloudSyncStrategy

    companion object {
        @Provides
        @Singleton
        fun provideFirebaseFirestore(): FirebaseFirestore {
            return FirebaseFirestore.getInstance()
        }

        @Provides
        @Singleton
        fun provideFirebaseStorage(): FirebaseStorage {
            return FirebaseStorage.getInstance()
        }
    }
}