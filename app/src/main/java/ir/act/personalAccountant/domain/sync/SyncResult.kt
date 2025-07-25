package ir.act.personalAccountant.domain.sync

sealed class SyncResult<T> {
    data class Success<T>(val data: T? = null) : SyncResult<T>()
    data class Error<T>(val message: String, val exception: Throwable? = null) : SyncResult<T>()
    data class Loading<T>(val progress: Int = 0) : SyncResult<T>()
}