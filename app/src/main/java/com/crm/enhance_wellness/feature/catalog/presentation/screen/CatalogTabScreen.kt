package com.crm.enhance_wellness.feature.catalog.presentation.screen

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.crm.enhance_wellness.core.ui.BackendImage
import com.crm.enhance_wellness.core.ui.EmptyState
import com.crm.enhance_wellness.core.ui.ErrorState
import com.crm.enhance_wellness.core.ui.WellnessCard
import com.crm.enhance_wellness.core.util.CurrencyUtil
import com.crm.enhance_wellness.core.util.DateUtil
import com.crm.enhance_wellness.feature.catalog.domain.model.Service
import com.crm.enhance_wellness.feature.catalog.domain.model.ServiceCategory
import com.crm.enhance_wellness.feature.catalog.domain.model.ServicePackage
import com.crm.enhance_wellness.feature.catalog.presentation.state.CatalogUiEvent
import com.crm.enhance_wellness.feature.catalog.presentation.state.CatalogUiState

private val TAB_LABELS = listOf("Services", "Packages", "Categories", "Memberships")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogTabScreen(
    state: CatalogUiState,
    onEvent: (CatalogUiEvent) -> Unit,
    membershipsContent: @Composable () -> Unit = {},
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
                    onClick = { selectedTab = index },
                    text = { Text(label) },
                )
            }
        }

        when (selectedTab) {
            0 -> ServiceCatalogContent(state = state, onEvent = onEvent)
            1 -> ServicePackagesContent(state = state, onEvent = onEvent)
            2 -> ServiceCategoriesContent(
                state = state,
                onEvent = { event ->
                    if (event is CatalogUiEvent.SelectCategory) {
                        onEvent(event)
                        selectedTab = 0
                    } else {
                        onEvent(event)
                    }
                },
            )
            3 -> membershipsContent()
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
    val filteredServices = remember(state.services, state.selectedCategoryId) {
        state.services.filter { service ->
            state.selectedCategoryId == null ||
                state.categories.find { it.id == state.selectedCategoryId }?.name == service.categoryName
        }
    }

    PullToRefreshBox(
        isRefreshing = state.isLoading,
        onRefresh = { onEvent(CatalogUiEvent.LoadServices) },
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            if (state.selectedCategoryId != null) {
                val activeName = state.categories.find { it.id == state.selectedCategoryId }?.name
                if (activeName != null) {
                    FilterChip(
                        selected = true,
                        onClick = { onEvent(CatalogUiEvent.ClearCategoryFilter) },
                        label = { Text("Showing: $activeName") },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear filter",
                                modifier = Modifier.size(14.dp),
                            )
                        },
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .widthIn(max = 280.dp),
                    )
                }
            }

            when {
                state.error != null -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    ErrorState(message = state.error, onRetry = { onEvent(CatalogUiEvent.LoadServices) })
                }

                filteredServices.isEmpty() && !state.isLoading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    EmptyState(message = "No services found.")
                }

                else -> LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 156.dp),
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
private fun ServiceCard(service: Service, onClick: () -> Unit) {
    WellnessCard(modifier = Modifier.fillMaxWidth(), onClick = onClick) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            BackendImage(
                imageUrl = service.imageUrl,
                contentDescription = service.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(108.dp),
                shape = RoundedCornerShape(10.dp),
                contentScale = ContentScale.Crop,
            ) {
                ServiceImageFallback(service = service)
            }

            Text(
                text = service.name,
                style = MaterialTheme.typography.titleSmall,
            )

            if (service.categoryName != null) {
                Spacer(modifier = Modifier.height(4.dp))
                SuggestionChip(
                    onClick = {},
                    label = { Text(service.categoryName, style = MaterialTheme.typography.labelSmall) },
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ServicePackagesContent(
    state: CatalogUiState,
    onEvent: (CatalogUiEvent) -> Unit,
) {
    val context = LocalContext.current
    LaunchedEffect(state.packageActionMessage) {
        state.packageActionMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            onEvent(CatalogUiEvent.DismissPackageMessage)
        }
    }

    PullToRefreshBox(
        isRefreshing = state.packagesLoading,
        onRefresh = { onEvent(CatalogUiEvent.LoadPackages) },
        modifier = Modifier.fillMaxSize(),
    ) {
        when {
            state.packagesError != null -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                ErrorState(message = state.packagesError, onRetry = { onEvent(CatalogUiEvent.LoadPackages) })
            }

            state.packages.isEmpty() && !state.packagesLoading -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                EmptyState(message = "No packages found.", icon = Icons.Default.Inventory2)
            }

            else -> LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                items(state.packages, key = { it.id }) { servicePackage ->
                    PackageCard(
                        servicePackage = servicePackage,
                        onBuy = { onEvent(CatalogUiEvent.BuyPackage(servicePackage.id)) },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PackageCard(servicePackage: ServicePackage, onBuy: () -> Unit) {
    WellnessCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Text(
                    text = servicePackage.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(8.dp))
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = if (servicePackage.isActive) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    },
                ) {
                    Text(
                        text = if (servicePackage.isActive) "Live" else "Inactive",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (servicePackage.isActive) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                }
            }

            if (!servicePackage.description.isNullOrBlank()) {
                Text(
                    text = servicePackage.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            if (servicePackage.serviceNames.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    servicePackage.serviceNames.forEach { name ->
                        SuggestionChip(onClick = {}, label = { Text(name, style = MaterialTheme.typography.labelSmall) })
                    }
                }
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = "${servicePackage.sessions} sessions",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (servicePackage.discountPercent > 0) {
                    Text(
                        text = "· ${servicePackage.discountPercent}% off",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                if (servicePackage.taxPercent > 0) {
                    Text(
                        text = "· +${servicePackage.taxPercent}% tax",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            if (!servicePackage.sellByDate.isNullOrBlank() || servicePackage.validityDays != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = buildString {
                            if (!servicePackage.sellByDate.isNullOrBlank()) {
                                append("Sell by ${DateUtil.toDisplayDate(servicePackage.sellByDate)}")
                            }
                            if (servicePackage.validityDays != null) {
                                if (isNotEmpty()) append(" · ")
                                append("valid ${servicePackage.validityDays} days after purchase")
                            }
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = CurrencyUtil.formatRupees(servicePackage.price),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                if (servicePackage.grossPrice != null && servicePackage.grossPrice > servicePackage.price) {
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = CurrencyUtil.formatRupees(servicePackage.grossPrice),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textDecoration = TextDecoration.LineThrough,
                        modifier = Modifier.padding(bottom = 3.dp),
                    )
                }
            }

            val owned = servicePackage.ownedPlan
            if (owned != null) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = "You own this package",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = "${owned.completedSessions}/${owned.totalSessions} sessions used",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    LinearProgressIndicator(
                        progress = {
                            if (owned.totalSessions > 0) {
                                (owned.completedSessions.toFloat() / owned.totalSessions).coerceIn(0f, 1f)
                            } else {
                                0f
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            } else {
                Button(
                    onClick = onBuy,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingBag,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Buy package")
                }
            }
        }
    }
}

@Composable
private fun ServiceDetailSheet(service: Service, onBook: () -> Unit, onDismiss: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(top = 8.dp, bottom = 28.dp),
    ) {
        BackendImage(
            imageUrl = service.imageUrl,
            contentDescription = service.name,
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            shape = RoundedCornerShape(12.dp),
            contentScale = ContentScale.Crop,
        ) {
            ServiceImageFallback(service = service)
        }
        Spacer(Modifier.height(16.dp))

        // Category label
        if (service.categoryName != null) {
            Text(
                text = service.categoryName.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.sp,
            )
            Spacer(Modifier.height(6.dp))
        }

        // Title + severity pill
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = service.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
            )
            val severity = when {
                (service.price ?: 0.0) >= 25000.0 -> Pair("HIGH", Color(0xFFE63946))
                (service.price ?: 0.0) >= 10000.0 -> Pair("MEDIUM", Color(0xFFE09B2D))
                else -> null
            }
            if (severity != null) {
                Spacer(Modifier.width(8.dp))
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = severity.second.copy(alpha = 0.15f),
                ) {
                    Text(
                        text = severity.first,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = severity.second,
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // 3-box stat row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ServiceStatBox(
                modifier = Modifier.weight(1f),
                label = "₹ BASE PRICE",
                value = if (service.price != null) "₹${service.price.toInt()}" else "—",
            )
            ServiceStatBox(
                modifier = Modifier.weight(1f),
                label = "DURATION",
                value = if (service.duration != null) "${service.duration} min" else "—",
            )
            ServiceStatBox(
                modifier = Modifier.weight(1f),
                label = "STATUS",
                value = if (service.isActive) "Active" else "Inactive",
            )
        }

        // Description
        if (!service.description.isNullOrBlank()) {
            Spacer(Modifier.height(16.dp))
            Text(
                text = "DESCRIPTION",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.sp,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = service.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Spacer(Modifier.height(20.dp))

        // Book service button
        Button(
            onClick = onBook,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.extraLarge,
        ) {
            Text("📅  Book service")
        }

        Spacer(Modifier.height(10.dp))

        // Footer
        Text(
            text = "Service ID: ${service.id}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(12.dp))

        // Got it / close
        OutlinedButton(
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.extraLarge,
        ) {
            Text("Got it")
        }
    }
}

@Composable
private fun ServiceStatBox(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                letterSpacing = 0.5.sp,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ServiceCategoriesContent(
    state: CatalogUiState,
    onEvent: (CatalogUiEvent) -> Unit,
) {
    PullToRefreshBox(
        isRefreshing = state.isLoading,
        onRefresh = { onEvent(CatalogUiEvent.LoadCategories) },
        modifier = Modifier.fillMaxSize(),
    ) {
        when {
            state.error != null -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                ErrorState(message = state.error, onRetry = { onEvent(CatalogUiEvent.LoadCategories) })
            }

            state.categories.isEmpty() && !state.isLoading -> Box(
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
                items(state.categories) { category ->
                    CategoryCard(
                        category = category,
                        onClick = { onEvent(CatalogUiEvent.SelectCategory(category.id)) },
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryCard(category: ServiceCategory, onClick: () -> Unit = {}) {
    WellnessCard(modifier = Modifier.fillMaxWidth(), onClick = onClick) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Image on the left — padded + rounded
            Box(
                modifier = Modifier
                    .padding(10.dp)
                    .width(90.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center,
            ) {
                if (!category.imageUrl.isNullOrBlank()) {
                    BackendImage(
                        imageUrl = category.imageUrl,
                        contentDescription = category.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        CategoryImageFallback(category = category)
                    }
                } else {
                    CategoryImageFallback(category = category)
                }
            }

            // Name + count to the right
            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
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
}

@Composable
private fun ServiceImageFallback(service: Service) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)),
        contentAlignment = Alignment.Center,
    ) {
        if (service.categoryName.isNullOrBlank()) {
            Icon(
                imageVector = Icons.Default.MedicalServices,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(38.dp),
            )
        } else {
            Text(
                text = service.categoryName.take(1).uppercase(),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}

@Composable
private fun CategoryImageFallback(category: ServiceCategory) {
    val bgColor = category.color?.let {
        runCatching { Color(android.graphics.Color.parseColor(it)) }.getOrNull()
    } ?: MaterialTheme.colorScheme.primaryContainer
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor),
        contentAlignment = Alignment.Center,
    ) {
        if (category.name.isBlank()) {
            Icon(
                imageVector = Icons.Default.Category,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp),
            )
        } else {
            Text(
                text = category.name.take(1).uppercase(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}
