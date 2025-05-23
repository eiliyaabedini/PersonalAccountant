package ir.act.personalAccountant.domain.usecase

import ir.act.personalAccountant.data.local.model.TagWithCount
import ir.act.personalAccountant.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetAllTagsUseCase @Inject constructor(
    private val repository: ExpenseRepository
) {
    operator fun invoke(): Flow<List<TagWithCount>> {
        return repository.getAllTagsWithCount().map { tags ->
            val mutableTags = tags.toMutableList()
            
            // Ensure "General" tag is always present
            if (mutableTags.none { it.tag == "General" }) {
                mutableTags.add(TagWithCount("General", 0))
            }
            
            // Sort: "General" first, then by count descending
            mutableTags.sortWith(compareBy(
                { it.tag != "General" },
                { -it.count }
            ))
            
            mutableTags
        }
    }
}