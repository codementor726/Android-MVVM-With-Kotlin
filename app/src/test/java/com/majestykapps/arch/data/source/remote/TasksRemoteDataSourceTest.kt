package com.majestykapps.arch.data.source.remote

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.majestykapps.arch.data.api.ApiResponse
import com.majestykapps.arch.data.api.TasksApiService
import com.majestykapps.arch.data.common.Resource
import com.majestykapps.arch.domain.entity.Task
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observer
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TasksRemoteDataSourceTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Mock
    private lateinit var api: TasksApiService

    private lateinit var dataSource: TasksRemoteDataSource

    @Captor
    private lateinit var resourceDataCaptor: ArgumentCaptor<Resource<List<Task>>>

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)

        dataSource = TasksRemoteDataSource.getInstance(api)
    }

    @After
    fun clearMocks() {
        // Ensures inline Kotlin mocks do not leak
        Mockito.framework().clearInlineMocks()
        TasksRemoteDataSource.destroy()
    }

    @Test
    fun `loading resource emitted when tasks are requested`() {
        val mock = mock<Call<ApiResponse<Task>>>()
        whenever(api.getTasks()).thenReturn(mock)

        val observer: Observer<Resource<List<Task>>> = mock()
        dataSource.getTasks().subscribe(observer)

        verify(observer, times(1)).onNext(resourceDataCaptor.capture())
        assertTrue(resourceDataCaptor.value is Resource.Loading)
        verify(observer, never()).onError(any())
    }

    @Test
    fun `api call cancelled when observable is disposed`() {
        val apiCall = mock<Call<ApiResponse<Task>>>()
        whenever(api.getTasks()).thenReturn(apiCall)

        val observer = dataSource.getTasks().test()
        observer.dispose()

        verify(apiCall, times(1)).cancel()
    }

    @Test
    fun `error emitted on api call failure`() {
        val apiCall = mock<Call<ApiResponse<Task>>>()
        whenever(api.getTasks()).thenReturn(apiCall)

        val exception = RuntimeException()
        whenever(apiCall.enqueue(any())).doAnswer {
            val callback: Callback<ApiResponse<Task>> = it.getArgument(0)
            callback.onFailure(apiCall, exception)
            null
        }

        val observer: Observer<Resource<List<Task>>> = mock()
        dataSource.getTasks().subscribe(observer)

        verify(observer, times(1)).onError(exception)
    }

    @Test
    fun `tasks emitted on api response success`() {
        val apiCall = mock<Call<ApiResponse<Task>>>()
        whenever(api.getTasks()).thenReturn(apiCall)

        val tasks = mock<List<Task>>()
        whenever(apiCall.enqueue(any())).doAnswer {
            val callback: Callback<ApiResponse<Task>> = it.getArgument(0)
            callback.onResponse(apiCall, Response.success(ApiResponse(true, "", tasks)))
            null
        }

        val observer: Observer<Resource<List<Task>>> = mock()
        dataSource.getTasks().subscribe(observer)

        verify(observer, times(2)).onNext(resourceDataCaptor.capture())
        val resource = resourceDataCaptor.allValues.last()
        assertTrue(resource is Resource.Success)
        assertEquals(resource.data, tasks)
    }

    @Test
    fun `error emitted on api response failure`() {
        val apiCall = mock<Call<ApiResponse<Task>>>()
        whenever(api.getTasks()).thenReturn(apiCall)

        val responseBody = ResponseBody.create("application/json".toMediaTypeOrNull(), "{}")

        whenever(apiCall.enqueue(any())).doAnswer {
            val callback: Callback<ApiResponse<Task>> = it.getArgument(0)
            callback.onResponse(apiCall, Response.error(404, responseBody))
            null
        }

        val observer = dataSource.getTasks().test()
        observer.assertError(RuntimeException::class.java)
    }

    @Test
    fun `error emitted from api response error message`() {
        val apiCall = mock<Call<ApiResponse<Task>>>()
        whenever(api.getTasks()).thenReturn(apiCall)

        val msg = "Test error message"
        whenever(apiCall.enqueue(any())).doAnswer {
            val callback: Callback<ApiResponse<Task>> = it.getArgument(0)
            callback.onResponse(apiCall, Response.success(ApiResponse(false, msg, listOf())))
            null
        }

        val observer = dataSource.getTasks().test()
        observer.assertErrorMessage(msg)
    }
}