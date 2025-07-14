package ir.act.personalAccountant.domain.usecase

import ir.act.personalAccountant.domain.repository.ExpenseRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MergeTagsUseCase @Inject constructor(
    private val expenseRepository: ExpenseRepository
) {
    suspend operator fun invoke(oldTags: List<String>, newTag: String): Int {
        return expenseRepository.mergeTags(oldTags, newTag)
    }
}