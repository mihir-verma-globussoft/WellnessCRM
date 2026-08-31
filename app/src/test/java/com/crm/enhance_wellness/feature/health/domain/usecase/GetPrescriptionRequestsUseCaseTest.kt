package com.crm.enhance_wellness.feature.health.domain.usecase

import com.crm.enhance_wellness.core.util.Result
import com.crm.enhance_wellness.feature.health.domain.model.PrescriptionRequest
import com.crm.enhance_wellness.feature.health.domain.repository.PrescriptionRequestRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class GetPrescriptionRequestsUseCaseTest {

    private lateinit var repository: PrescriptionRequestRepository
    private lateinit var useCase: GetPrescriptionRequestsUseCase

    private val fakeRequest = PrescriptionRequest(
        id = 5,
        status = "PENDING",
        prescriptionId = 367,
        doctorName = "Pratibha Laxmi Singh",
        isFullPrescription = false,
        requestedDrugNames = listOf("Azithromycin 500mg"),
        prescriptionDrugNames = listOf("Azithromycin 500mg", "Biotin 10000mcg"),
        requestedDurationDays = 60,
        notes = "Running low, please repeat just the tablets.",
        reviewedByName = null,
        reviewedAt = null,
        reviewNote = null,
        createdAt = "2026-08-24T10:23:05.744Z",
    )

    @BeforeEach
    fun setUp() {
        repository = mockk()
        useCase = GetPrescriptionRequestsUseCase(repository)
    }

    @Test
    fun `returns Success with request list when API call succeeds`() = runTest {
        coEvery { repository.getPrescriptionRequests() } returns listOf(fakeRequest)

        val result = useCase()

        assertTrue(result is Result.Success)
        assertEquals(listOf(fakeRequest), (result as Result.Success).data)
    }

    @Test
    fun `returns UNAUTHORIZED on 401`() = runTest {
        val response = Response.error<List<PrescriptionRequest>>(401, "".toResponseBody())
        coEvery { repository.getPrescriptionRequests() } throws HttpException(response)

        val result = useCase()

        assertTrue(result is Result.Error)
        assertEquals("UNAUTHORIZED", (result as Result.Error).code)
        assertEquals(401, result.httpStatus)
    }

    @Test
    fun `returns FORBIDDEN on 403`() = runTest {
        val response = Response.error<List<PrescriptionRequest>>(403, "".toResponseBody())
        coEvery { repository.getPrescriptionRequests() } throws HttpException(response)

        val result = useCase()

        assertTrue(result is Result.Error)
        assertEquals("FORBIDDEN", (result as Result.Error).code)
        assertEquals(403, result.httpStatus)
    }

    @Test
    fun `returns generic HTTP error for other status codes`() = runTest {
        val response = Response.error<List<PrescriptionRequest>>(500, "".toResponseBody())
        coEvery { repository.getPrescriptionRequests() } throws HttpException(response)

        val result = useCase()

        assertTrue(result is Result.Error)
        assertEquals("HTTP_500", (result as Result.Error).code)
    }

    @Test
    fun `returns NETWORK_ERROR when IOException thrown`() = runTest {
        coEvery { repository.getPrescriptionRequests() } throws IOException()

        val result = useCase()

        assertTrue(result is Result.Error)
        assertEquals("NETWORK_ERROR", (result as Result.Error).code)
    }
}
