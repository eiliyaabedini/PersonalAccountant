package ir.act.personalAccountant.domain.usecase

import android.net.Uri
import ir.act.personalAccountant.domain.model.AssetAnalysisResult

interface AssetImageAnalysisUseCase {
    /**
     * Analyzes a screenshot from a trading app to extract asset information
     * @param imageUri The URI of the image to analyze
     * @return AssetAnalysisResult containing extracted asset data
     */
    suspend fun analyzeAssetImage(imageUri: Uri): Result<AssetAnalysisResult>

    /**
     * Verifies if the analyzed asset matches an existing asset (for updates)
     * @param existingAssetName The name of the existing asset
     * @param analyzedResult The result from image analysis
     * @return Boolean indicating if they match
     */
    suspend fun verifyAssetMatch(
        existingAssetName: String,
        analyzedResult: AssetAnalysisResult
    ): Boolean
}