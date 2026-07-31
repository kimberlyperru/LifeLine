package com.perru.lifeline.presentation.hospital

import android.net.Uri
import app.cash.turbine.test
import com.perru.lifeline.data.remote.CloudinaryUploader
import com.perru.lifeline.domain.model.BloodRequest
import com.perru.lifeline.domain.model.LifeLineUser
import com.perru.lifeline.domain.repository.AuthRepository
import com.perru.lifeline.domain.repository.RequestRepository
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HospitalViewModelTest {

    private val requestRepository: RequestRepository = mockk()
    private val authRepository: AuthRepository = mockk()
    private val cloudinaryUploader: CloudinaryUploader = mockk()

    private lateinit var viewModel: HospitalViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { authRepository.currentUserFlow() } returns flowOf(null)
        viewModel = HospitalViewModel(requestRepository, authRepository, cloudinaryUploader)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `submitRequest success with image upload`() = runTest {
        val hospital = LifeLineUser(uid = "h1", hospitalName = "City Hospital", hospitalVerified = true)
        val request = BloodRequest(notes = "Urgent")
        val imageUri: Uri = mockk()
        val imageUrl = "https://cloudinary.com/image.jpg"

        coEvery { cloudinaryUploader.uploadImage(imageUri) } returns Result.success(imageUrl)
        coEvery { requestRepository.createRequest(any()) } returns Result.success("r1")

        viewModel.createState.test {
            skipItems(1) // Initial state

            viewModel.submitRequest(hospital, request, imageUri)

            // Expecting sequence of states
            assertEquals(CreateRequestState(isSubmitting = true), awaitItem())
            assertEquals(CreateRequestState(isSubmitting = true, isUploading = true), awaitItem())
            assertEquals(CreateRequestState(isSubmitting = true, isUploading = false), awaitItem())
            assertEquals(CreateRequestState(submitted = true), awaitItem())
        }

        coVerify { cloudinaryUploader.uploadImage(imageUri) }
    }

    @Test
    fun `submitRequest failure when image upload fails`() = runTest {
        val hospital = LifeLineUser(uid = "h1")
        val request = BloodRequest()
        val imageUri: Uri = mockk()

        coEvery { cloudinaryUploader.uploadImage(imageUri) } returns Result.failure(Exception("Upload error"))

        viewModel.createState.test {
            skipItems(1) // Initial state

            viewModel.submitRequest(hospital, request, imageUri)

            assertEquals(CreateRequestState(isSubmitting = true), awaitItem())
            assertEquals(CreateRequestState(isSubmitting = true, isUploading = true), awaitItem())
            val errorState = awaitItem()
            assertTrue(errorState.errorMessage?.contains("Upload error") == true)
        }

        coVerify(exactly = 0) { requestRepository.createRequest(any()) }
    }
}
