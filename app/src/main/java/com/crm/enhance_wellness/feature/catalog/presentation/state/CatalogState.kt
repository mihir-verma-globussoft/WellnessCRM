package com.crm.enhance_wellness.feature.catalog.presentation.state

import com.crm.enhance_wellness.feature.catalog.domain.model.Service
import com.crm.enhance_wellness.feature.catalog.domain.model.ServiceCategory
import com.crm.enhance_wellness.feature.catalog.domain.model.ServicePackage

data class CatalogUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val services: List<Service> = emptyList(),
    val categories: List<ServiceCategory> = emptyList(),
    val searchQuery: String = "",
    val selectedCategoryId: Int? = null,
    val selectedService: Service? = null,    // shown in bottom sheet
    val showServiceDetail: Boolean = false,
    val packages: List<ServicePackage> = emptyList(),
    val packagesLoading: Boolean = false,
    val packagesError: String? = null,
    // One-shot message for actions not implemented yet (e.g. "Buy package" — read-only for now).
    val packageActionMessage: String? = null,
)

sealed class CatalogUiEvent {
    object LoadServices : CatalogUiEvent()
    object LoadCategories : CatalogUiEvent()
    object LoadPackages : CatalogUiEvent()
    data class UpdateSearch(val query: String) : CatalogUiEvent()
    data class SelectCategory(val categoryId: Int?) : CatalogUiEvent()
    object ClearCategoryFilter : CatalogUiEvent()
    data class ShowServiceDetail(val service: Service) : CatalogUiEvent()
    object DismissServiceDetail : CatalogUiEvent()
    data class BookService(val serviceId: Int) : CatalogUiEvent()
    data class BuyPackage(val packageId: Int) : CatalogUiEvent()
    object DismissPackageMessage : CatalogUiEvent()
}
