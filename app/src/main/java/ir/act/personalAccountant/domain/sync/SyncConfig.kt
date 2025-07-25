package ir.act.personalAccountant.domain.sync

data class SyncConfig(
    val isEnabled: Boolean = true,
    val autoSync: Boolean = true,
    val wifiOnly: Boolean = false
)