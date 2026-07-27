package com.crm.enhance_wellness.feature.health.presentation.viewmodel

import app.cash.turbine.test
import com.crm.enhance_wellness.core.util.Result
import com.crm.enhance_wellness.feature.auth.domain.model.PatientPermissions
import com.crm.enhance_wellness.feature.auth.domain.usecase.GetPatientPermissionsUseCase
import com.crm.enhance_wellness.feature.health.domain.model.Prescription
import com.crm.enhance_wellness.feature.health.domain.usecase.GetPrescriptionsUseCase
import com.crm.enhance_wellness.feature.health.presentation.state.PrescriptionsUiEvent
import com.crm.enhance_wellness.feature.health.reminder.PrescriptionReminderRepository
import io.mockk.coJustRun
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PrescriptionsViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var getPrescriptionsUseCase: GetPrescriptionsUseCase
    private lateinit var getPermissionsUseCase: GetPatientPermissionsUseCase
    private lateinit var reminderRepository: PrescriptionReminderRepository
    private lateinit var vm: PrescriptionsViewModel

    private val fakePrescription = Prescription(
        id = 1,
        visitId = 10,
        visitDate = "2026-05-01",
        createdAt = "2026-05-01T00:00:00.000Z",
        instructions = null,
        doctorName = "Dr. Test",
        serviceName = null,
        drugs = emptyList(),
    )

    private val permittedPermissions = PatientPermissions(setOf(PatientPermissions.PRESCRIPTIONS_READ))

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        getPrescriptionsUseCase = mockk()
        getPermissionsUseCase = mockk()
        reminderRepository = mockk()
        coEvery { getPermissionsUseCase() } returns Result.Success(permittedPermissions)
        coJustRun { reminderRepository.cleanupExpired() }
        every { reminderRepository.enabledReminderIds() } returns flowOf(emptySet())
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init loads prescriptions into success state`() = runTest {
        coEvery { getPrescriptionsUseCase() } returns Result.Success(listOf(fakePrescription))

        vm = PrescriptionsViewModel(getPrescriptionsUseCase, getPermissionsUseCase, reminderRepository)

        assertFalse(vm.uiState.value.isLoading)
        assertNull(vm.uiState.value.error)
        assertEquals(listOf(fakePrescription), vm.uiState.value.prescriptions)
    }

    @Test
    fun `init sets error state when getPrescriptionsUseCase returns Error`() = runTest {
        coEvery { getPrescriptionsUseCase() } returns Result.Error("HTTP_401", "Unauthorized", 401)

        vm = PrescriptionsViewModel(getPrescriptionsUseCase, getPermissionsUseCase, reminderRepository)

        assertFalse(vm.uiState.value.isLoading)
        assertNotNull(vm.uiState.value.error)
        assertTrue(vm.uiState.value.prescriptions.isEmpty())
    }

    @Test
    fun `Refresh event reloads data with updated result`() = runTest {
        coEvery { getPrescriptionsUseCase() } returns Result.Error("NETWORK_ERROR", "No connection")
        vm = PrescriptionsViewModel(getPrescriptionsUseCase, getPermissionsUseCase, reminderRepository)
        assertNotNull(vm.uiState.value.error)

        coEvery { getPrescriptionsUseCase() } returns Result.Success(listOf(fakePrescription))
        vm.onEvent(PrescriptionsUiEvent.Refresh)

        assertFalse(vm.uiState.value.isLoading)
        assertNull(vm.uiState.value.error)
        assertEquals(listOf(fakePrescription), vm.uiState.value.prescriptions)
    }

    @Test
    fun `ViewPdf event emits ToPdf nav event with correct id`() = runTest {
        coEvery { getPrescriptionsUseCase() } returns Result.Success(listOf(fakePrescription))
        vm = PrescriptionsViewModel(getPrescriptionsUseCase, getPermissionsUseCase, reminderRepository)

        vm.navEvent.test {
            vm.onEvent(PrescriptionsUiEvent.ViewPdf(prescriptionId = 1))
            assertEquals(PrescriptionsNavEvent.ToPdf(1), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `StartTreatmentAnalysis event emits ToAnalysis nav event with prescription and visit id`() = runTest {
        coEvery { getPrescriptionsUseCase() } returns Result.Success(listOf(fakePrescription))
        vm = PrescriptionsViewModel(getPrescriptionsUseCase, getPermissionsUseCase, reminderRepository)

        vm.navEvent.test {
            vm.onEvent(PrescriptionsUiEvent.StartTreatmentAnalysis(prescriptionId = 1))
            assertEquals(PrescriptionsNavEvent.ToAnalysis(prescriptionId = 1, visitId = 10), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `NavigateBack event emits Back nav event`() = runTest {
        coEvery { getPrescriptionsUseCase() } returns Result.Success(emptyList())
        vm = PrescriptionsViewModel(getPrescriptionsUseCase, getPermissionsUseCase, reminderRepository)

        vm.navEvent.test {
            vm.onEvent(PrescriptionsUiEvent.NavigateBack)
            assertEquals(PrescriptionsNavEvent.Back, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `sets permissionBlocked when my_prescriptions_read is absent`() = runTest {
        coEvery { getPermissionsUseCase() } returns Result.Success(PatientPermissions.EMPTY)

        vm = PrescriptionsViewModel(getPrescriptionsUseCase, getPermissionsUseCase, reminderRepository)

        assertFalse(vm.uiState.value.isLoading)
        assertTrue(vm.uiState.value.permissionBlocked)
        assertTrue(vm.uiState.value.prescriptions.isEmpty())
        assertNull(vm.uiState.value.error)
    }
}
