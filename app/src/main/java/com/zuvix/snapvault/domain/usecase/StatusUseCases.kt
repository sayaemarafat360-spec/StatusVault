package com.zuvix.snapvault.domain.usecase

import com.zuvix.snapvault.data.model.StatusItem
import com.zuvix.snapvault.data.repository.StatusRepository
import javax.inject.Inject

class LoadStatusesUseCase @Inject constructor(
    private val repository: StatusRepository
) {
    suspend operator fun invoke(): List<StatusItem> {
        return repository.loadStatuses()
    }
}

class RefreshStatusesUseCase @Inject constructor(
    private val repository: StatusRepository
) {
    suspend operator fun invoke(): Pair<List<StatusItem>, Int> {
        return repository.refreshStatuses()
    }
}

class SaveStatusUseCase @Inject constructor(
    private val repository: StatusRepository
) {
    suspend operator fun invoke(status: StatusItem, toVault: Boolean = false): Boolean {
        val uri = repository.saveStatus(status, toVault)
        return uri != null
    }
}

class SaveMultipleStatusesUseCase @Inject constructor(
    private val repository: StatusRepository
) {
    suspend operator fun invoke(statuses: List<StatusItem>, toVault: Boolean = false): Int {
        return repository.saveMultipleStatuses(statuses, toVault)
    }
}

class ToggleFavoriteUseCase @Inject constructor(
    private val repository: StatusRepository
) {
    suspend operator fun invoke(savedStatusId: String) {
        repository.getSavedStatusById(savedStatusId)?.let {
            repository.toggleFavorite(it)
        }
    }
}

class MoveToVaultUseCase @Inject constructor(
    private val repository: StatusRepository
) {
    suspend operator fun invoke(savedStatusId: String): Boolean {
        return repository.getSavedStatusById(savedStatusId)?.let {
            repository.moveToVault(it)
        } ?: false
    }
}

class DeleteSavedStatusUseCase @Inject constructor(
    private val repository: StatusRepository
) {
    suspend operator fun invoke(savedStatusId: String): Boolean {
        return repository.getSavedStatusById(savedStatusId)?.let {
            repository.deleteSavedStatus(it)
        } ?: false
    }
}
