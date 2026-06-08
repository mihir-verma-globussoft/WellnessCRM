package com.globussoft.wellness.patient.feature.catalog.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.globussoft.wellness.patient.core.ui.EmptyState
import com.globussoft.wellness.patient.core.ui.ErrorState
import com.globussoft.wellness.patient.core.ui.WellnessCard
import com.globussoft.wellness.patient.feature.catalog.domain.model.Service
import com.globussoft.wellness.patient.feature.catalog.domain.model.ServiceCategory
import com.globussoft.wellness.patient.feature.catalog.presentation.state.CatalogUiEvent
import com.globussoft.wellness.patient.feature.catalog.presentation.state.CatalogUiState

private val TAB_LABELS = listOf("Services", "Categories", "Memberships")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogTabScreen(
    state: CatalogUiState,
    onEvent: (CatalogUiEvent) -> Unit,
    onNavigateToMemberships: () -> Unit = {},
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            edgePadding = 0.dp,
        ) {
            TAB_LABELS.forEachIndexed { index, label ->
                Tab(
                    selected = selectedTab == index,
                    onClick = {
                        if (index == 2) {
                            onNavigateToMemberships()
                        } else {
                            selectedTab = index
                        }
                    },
                    text = { Text(label) },
                )
            }
        }

        when (selectedTab) {
            0 -> ServiceCatalogContent(
                state = state,
                onEvent = onEvent,
            )
            1 -> ServiceCategoriesContent(
                state = state,
                onEvent = onEvent,
            )
        }
    }

    // Service detail bottom sheet
    if (state.showServiceDetail && state.selectedService != null) {
        ModalBottomSheet(
            onDismissRequest = { onEvent(CatalogUiEvent.DismissServiceDetail) },
            sheetState = sheetState,
        ) {
            ServiceDetailSheet(
                service = state.selectedService,
                onBook = { onEvent(CatalogUiEvent.BookService(state.selectedService.id)) },
                onDismiss = { onEvent(CatalogUiEvent.DismissServiceDetail) },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ServiceCatalogContent(
    state: CatalogUiState,
    onEvent: (CatalogUiEvent) -> Unit,
) {
    val filteredServices = remember(state.services, state.searchQuery, state.selectedCategoryId) {
        state.services.filter { service ->
            val matchesSearch = state.searchQuery.isBlank() ||
                service.name.contains(state.searchQuery, ignoreCase = true) ||
                service.description?.contains(state.searchQuery, ignoreCase = true) == true
            val matchesCategory = state.selectedCategoryId == null ||
                state.categories.find { it.id == state.selectedCategoryId }?.name == service.categoryName
            matchesSearch && matchesCategory
        }
    }

    PullToRefreshBox(
        isRefreshing = state.isLoading,
        onRefresh = { onEvent(CatalogUiEvent.LoadServices) },
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Search field
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { onEvent(CatalogUiEvent.UpdateSearch(it)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                placeholder = { Text("Search services…") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
            )

            when {
                state.error != null -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    ErrorState(
                        message = state.error,
                        onRetry = { onEvent(CatalogUiEvent.LoadServices) },
                    )
                }

                filteredServices.isEmpty() && !state.isLoading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    EmptyState(message = "No services found.")
                }

                else -> LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(filteredServices) { service ->
                        ServiceCard(
                            service = service,
                            onClick = { onEvent(CatalogUiEvent.ShowServiceDetail(service)) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ServiceCard(
    service: Service,
    onClick: () -> Unit,
) {
    WellnessCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = service.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
            )

            if (service.categoryName != null) {
                Spacer(modifier = Modifier.height(4.dp))
                SuggestionChip(
                    onClick = {},
                    label = {
                        Text(
                            text = service.categoryName,
                            style = MaterialTheme.typography.labelSmall,
                        )
                    },
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            if (service.discountedPrice != null) {
                Text(
                    text = "₹${service.discountedPrice.toInt()}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = "₹${service.price?.toInt() ?: ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else if (service.price != null) {
                Text(
                    text = "₹${service.price.toInt()}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            if (service.duration != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${service.duration} min",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun ServiceDetailSheet(
    service: Service,
    onBook: () -> Unit,
    onDismiss: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
    ) {
        Text(
            text = service.name,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
        )

        if (service.categoryName != null) {
            Spacer(modifier = Modifier.height(6.dp))
            AssistChip(
                onClick = {},
                label = { Text(service.categoryName) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Category,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                    )
                },
            )
        }

        if (service.description != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = service.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                if (service.discountedPrice != null) {
                    Text(
                        text = "₹${service.discountedPrice.toInt()}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = "₹${service.price?.toInt() ?: ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else if (service.price != null) {
                    Text(
                        text = "₹${service.price.toInt()}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                } else {
                    Text(
                        text = "Price on consultation",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                if (service.duration != null) {
                    Text(
                        text = "${service.duration} min session",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Button(
                onClick = onBook,
                shape = MaterialTheme.shapes.extraLarge,
            ) {
                Text("Book this service")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ServiceCategoriesContent(
    state: CatalogUiState,
    onEvent: (CatalogUiEvent) -> Unit,
) {
    val filteredCategories = remember(state.categories, state.searchQuery) {
        state.categories.filter { category ->
            state.searchQuery.isBlank() ||
                category.name.contains(state.searchQuery, ignoreCase = true)
        }
    }

    PullToRefreshBox(
        isRefreshing = state.isLoading,
        onRefresh = { onEvent(CatalogUiEvent.LoadCategories) },
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Search field
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { onEvent(CatalogUiEvent.UpdateSearch(it)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                placeholder = { Text("Search categories…") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
            )

            when {
                state.error != null -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    ErrorState(
                        message = state.error,
                        onRetry = { onEvent(CatalogUiEvent.LoadCategories) },
                    )
                }

                filteredCategories.isEmpty() && !state.isLoading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    EmptyState(message = "No categories found.")
                }

                else -> LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(filteredCategories) { category ->
                        CategoryCard(category = category)
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryCard(
    category: ServiceCategory,
) {
    WellnessCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = category.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f),
            )
            Spacer(modifier = Modifier.width(8.dp))
            SuggestionChip(
                onClick = {},
                label = {
                    Text(
                        text = "${category.servicesCount} services",
                        style = MaterialTheme.typography.labelSmall,
                    )
                },
            )
        }
    }
}
