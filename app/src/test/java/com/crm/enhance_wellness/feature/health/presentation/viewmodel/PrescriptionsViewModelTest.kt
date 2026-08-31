package com.crm.enhance_wellness.feature.health.presentation.viewmodel

import app.cash.turbine.test
import com.crm.enhance_wellness.core.util.Result
import com.crm.enhance_wellness.feature.auth.domain.model.PatientPermissions
import com.crm.enhance_wellness.feature.auth.domain.usecase.GetPatientPermissionsUseCase
import com.crm.enhance_wellness.feature.health.domain.model.Prescription
import com.crm.enhance_wellness.feature.health.domain.usecase.GetPrescriptionRequestsUseCase
import com.crm.enhance_wellness.feature.health.domain.usecase.GetPrescriptionsUseCase
import com.crm.enhance_wellness.feature.health.domain.usecase.SubmitPrescriptionRenewalUseCase
import com.crm.enhance_wellness.feature.health.presentation.state.PrescriptionsUiEvent
import com.crm.enhance_wellness.feature.health.reminder.PrescriptionReminderRepository
import io.mockk.coJustRun
import io.mockk.coEvery
import io.mockk.coVerify
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
    private lateinit var getPrescriptionRequestsUseCase: GetPrescriptionRequestsUseCase
    private lateinit var submitPrescriptionRenewalUseCase: SubmitPrescriptionRenewalUseCase
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
        getPrescriptionRequestsUseCase = mockk()
        submitPrescriptionRenewalUseCase = mockk()
        coEvery { getPermissionsUseCase() } returns Result.Success(permittedPermissions)
        coJustRun { reminderRepository.cleanupExpired() }
        every { reminderRepository.enabledReminderIds() } returns flowOf(emptySet())
        coEvery { getPrescriptionRequestsUseCase() } returns Result.Success(emptyList())
    }

    private fun buildViewModel() = PrescriptionsViewModel(
        getPrescriptionsUseCase,
        getPermissionsUseCase,
        reminderRepository,
        getPrescriptionRequestsUseCase,
        submitPrescriptionRenewalUseCase,
    )

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init loads prescriptions into success state`() = runTest {
        coEvery { getPrescriptionsUseCase() } returns Result.Success(listOf(fakePrescription))

        vm = buildViewModel()

        assertFalse(vm.uiState.value.isLoading)
        assertNull(vm.uiState.value.error)
        assertEquals(listOf(fakePrescription), vm.uiState.value.prescriptions)
    }

    @Test
    fun `init sets error state when getPrescriptionsUseCase returns Error`() = runTest {
        coEvery { getPrescriptionsUseCase() } returns Result.Error("HTTP_401", "Unauthorized", 401)

        vm = buildViewModel()

        assertFalse(vm.uiState.value.isLoading)
        assertNotNull(vm.uiState.value.error)
        assertTrue(vm.uiState.value.prescriptions.isEmpty())
    }

    @Test
    fun `Refresh event reloads data with updated result`() = runTest {
        coEvery { getPrescriptionsUseCase() } returns Result.Error("NETWORK_ERROR", "No connection")
        vm = buildViewModel()
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
        vm = buildViewModel()

        vm.navEvent.test {
            vm.onEvent(PrescriptionsUiEvent.ViewPdf(prescriptionId = 1))
            assertEquals(PrescriptionsNavEvent.ToPdf(1), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `StartTreatmentAnalysis event emits ToAnalysis nav event with prescription and visit id`() = runTest {
        coEvery { getPrescriptionsUseCase() } returns Result.Success(listOf(fakePrescription))
        vm = buildViewModel()

        vm.navEvent.test {
            vm.onEvent(PrescriptionsUiEvent.StartTreatmentAnalysis(prescriptionId = 1))
            assertEquals(PrescriptionsNavEvent.ToAnalysis(prescriptionId = 1, visitId = 10), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `NavigateBack event emits Back nav event`() = runTest {
        coEvery { getPrescriptionsUseCase() } returns Result.Success(emptyList())
        vm = buildViewModel()

        vm.navEvent.test {
            vm.onEvent(PrescriptionsUiEvent.NavigateBack)
            assertEquals(PrescriptionsNavEvent.Back, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `sets permissionBlocked when my_prescriptions_read is absent`() = runTest {
        coEvery { getPermissionsUseCase() } returns Result.Success(PatientPermissions.EMPTY)

        vm = buildViewModel()

        assertFalse(vm.uiState.value.isLoading)
        assertTrue(vm.uiState.value.permissionBlocked)
        assertTrue(vm.uiState.value.prescriptions.isEmpty())
        assertNull(vm.uiState.value.error)
    }

    @Test
    fun `init loads open renewal request ids, filtering to PENDING and ACCEPTED only`() = runTest {
        coEvery { getPrescriptionsUseCase() } returns Result.Success(listOf(fakePrescription))
        coEvery { getPrescriptionRequestsUseCase() } returns Result.Success(
            listOf(
                fakeRequest(id = 1, prescriptionId = 1, status = "PENDING"),
                fakeRequest(id = 2, prescriptionId = 2, status = "REJECTED"),
                fakeRequest(id = 3, prescriptionId = 3, status = "ACCEPTED"),
            )
        )

        vm = buildViewModel()

        assertEquals(setOf(1, 3), vm.uiState.value.openRenewalPrescriptionIds)
    }

    @Test
    fun `RequestRenewal event opens the dialog for the given prescription`() = runTest {
        coEvery { getPrescriptionsUseCase() } returns Result.Success(listOf(fakePrescription))
        vm = buildViewModel()

        vm.onEvent(PrescriptionsUiEvent.RequestRenewal(fakePrescription))

        assertEquals(fakePrescription, vm.uiState.value.renewalDialogPrescription)
    }

    @Test
    fun `DismissRenewalDialog event clears the dialog`() = runTest {
        coEvery { getPrescriptionsUseCase() } returns Result.Success(listOf(fakePrescription))
        vm = buildViewModel()
        vm.onEvent(PrescriptionsUiEvent.RequestRenewal(fakePrescription))

        vm.onEvent(PrescriptionsUiEvent.DismissRenewalDialog)

        assertNull(vm.uiState.value.renewalDialogPrescription)
    }

    @Test
    fun `SubmitRenewal on success closes the dialog, shows a message and reloads open requests`() = runTest {
        coEvery { getPrescriptionsUseCase() } returns Result.Success(listOf(fakePrescription))
        coEvery {
            submitPrescriptionRenewalUseCase(prescriptionId = 1, medicines = null, durationDays = 30, notes = "please")
        } returns Result.Success(Unit)
        vm = buildViewModel()
        vm.onEvent(PrescriptionsUiEvent.RequestRenewal(fakePrescription))
        coEvery { getPrescriptionRequestsUseCase() } returns Result.Success(
            listOf(fakeRequest(id = 9, prescriptionId = 1, status = "PENDING"))
        )

        vm.onEvent(PrescriptionsUiEvent.SubmitRenewal(fullPrescription = true, selectedMedicines = emptySet(), durationDays = 30, notes = "please"))

        assertNull(vm.uiState.value.renewalDialogPrescription)
        assertFalse(vm.uiState.value.renewalSubmitInProgress)
        assertEquals("Renewal request sent.", vm.uiState.value.renewalMessage)
        assertEquals(setOf(1), vm.uiState.value.openRenewalPrescriptionIds)
    }

    @Test
    fun `SubmitRenewal for specific medicines passes them and omits medicines when full prescription`() = runTest {
        coEvery { getPrescriptionsUseCase() } returns Result.Success(listOf(fakePrescription))
        coEvery {
            submitPrescriptionRenewalUseCase(prescriptionId = 1, medicines = listOf("Amoxicillin"), durationDays = null, notes = null)
        } returns Result.Success(Unit)
        vm = buildViewModel()
        vm.onEvent(PrescriptionsUiEvent.RequestRenewal(fakePrescription))

        vm.onEvent(
            PrescriptionsUiEvent.SubmitRenewal(
                fullPrescription = false,
                selectedMedicines = setOf("Amoxicillin"),
                durationDays = null,
                notes = null,
            )
        )

        coVerify(exactly = 1) {
            submitPrescriptionRenewalUseCase(prescriptionId = 1, medicines = listOf("Amoxicillin"), durationDays = null, notes = null)
        }
    }

    @Test
    fun `SubmitRenewal on error keeps the dialog open and shows the error message`() = runTest {
        coEvery { getPrescriptionsUseCase() } returns Result.Success(listOf(fakePrescription))
        coEvery {
            submitPrescriptionRenewalUseCase(prescriptionId = 1, medicines = null, durationDays = null, notes = null)
        } returns Result.Error("HTTP_500", "Server error")
        vm = buildViewModel()
        vm.onEvent(PrescriptionsUiEvent.RequestRenewal(fakePrescription))

        vm.onEvent(PrescriptionsUiEvent.SubmitRenewal(fullPrescription = true, selectedMedicines = emptySet(), durationDays = null, notes = null))

        assertEquals(fakePrescription, vm.uiState.value.renewalDialogPrescription)
        assertFalse(vm.uiState.value.renewalSubmitInProgress)
        assertEquals("Server error", vm.uiState.value.renewalMessage)
    }

    private fun fakeRequest(id: Int, prescriptionId: Int, status: String) =
        com.crm.enhance_wellness.feature.health.domain.model.PrescriptionRequest(
            id = id,
            status = status,
            prescriptionId = prescriptionId,
            doctorName = null,
            isFullPrescription = true,
            requestedDrugNames = emptyList(),
            prescriptionDrugNames = emptyList(),
            requestedDurationDays = null,
            notes = null,
            reviewedByName = null,
            reviewedAt = null,
            reviewNote = null,
            createdAt = null,
        )
}
