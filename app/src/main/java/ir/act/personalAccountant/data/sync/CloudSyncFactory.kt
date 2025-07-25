package ir.act.personalAccountant.data.sync

import ir.act.personalAccountant.domain.sync.CloudSyncStrategy
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Configuration for cloud sync behavior
 */
data class SyncConfig(
    val provider: CloudProvider = CloudProvider.FIREBASE,
    val autoSyncEnabled: Boolean = true,
    val batchSyncSize: Int = 50,
    val retryAttempts: Int = 3
)

/**
 * Supported cloud sync providers
 */
enum class CloudProvider {
    FIREBASE,
    // Future providers can be added here:
    // AZURE,
    // AWS,
    // GOOGLE_DRIVE,
    // CUSTOM
}

/**
 * Factory for creating cloud sync strategies.
 * This provides extensibility for future cloud providers while maintaining a clean interface.
 */
@Singleton
class CloudSyncFactory @Inject constructor(
    private val firebaseSyncStrategy: FirebaseSyncStrategy,
    private val syncConfig: SyncConfig = SyncConfig() // Default config
) {

    /**
     * Creates the appropriate cloud sync strategy based on configuration.
     * Currently always returns Firebase strategy, but can be extended for multiple providers.
     */
    fun createSyncStrategy(): CloudSyncStrategy {
        // For now, always return Firebase strategy
        // In future, this can be configurable:
        return when (syncConfig.provider) {
            CloudProvider.FIREBASE -> firebaseSyncStrategy
            // Future implementations:
            // CloudProvider.AZURE -> azureSyncStrategy
            // CloudProvider.AWS -> awsSyncStrategy
            // CloudProvider.GOOGLE_DRIVE -> googleDriveSyncStrategy
            // CloudProvider.CUSTOM -> customSyncStrategy
        }
    }

    /**
     * Gets the current sync configuration
     */
    fun getSyncConfig(): SyncConfig = syncConfig

    /**
     * Updates sync configuration (useful for runtime configuration changes)
     */
    fun updateSyncConfig(newConfig: SyncConfig): CloudSyncFactory {
        return CloudSyncFactory(firebaseSyncStrategy, newConfig)
    }

    /**
     * Checks if the current provider supports batch sync operations
     */
    fun supportsBatchSync(): Boolean {
        return when (syncConfig.provider) {
            CloudProvider.FIREBASE -> true
            // Future providers can define their capabilities
        }
    }

    /**
     * Checks if the current provider supports image upload
     */
    fun supportsImageUpload(): Boolean {
        return when (syncConfig.provider) {
            CloudProvider.FIREBASE -> true
            // Future providers can define their capabilities
        }
    }
}