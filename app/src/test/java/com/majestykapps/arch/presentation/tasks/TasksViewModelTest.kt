package com.majestykapps.arch.presentation.tasks

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.majestykapps.arch.data.common.Resource
import com.majestykapps.arch.domain.entity.Task
import com.majestykapps.arch.domain.usecase.SubscribeTasksUseCase
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations

class TasksViewModelTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Mock
    lateinit var useCase: SubscribeTasksUseCase

    lateinit var viewModel: TasksViewModel

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)

        viewModel = TasksViewModel(useCase)
    }

    @After
    fun clearMocks() {
        // Ensures inline Kotlin mocks do not leak
        Mockito.framework().clearInlineMocks()
    }

    @Test
    fun `subscribes to use case on init`() {
        verify(useCase, times(1)).subscribe(any())
        verify(useCase, times(1)).load()
    }

    @Test
    fun `refreshes use case on refresh`() {
        viewModel.refresh()
        verify(useCase, times(1)).refresh()
    }

    @Test
    fun `launch event triggered when task clicked`() {
        val observer: Observer<String> = mock()
        viewModel.launchEvent.observeForever(observer)

        val id = "id"
        viewModel.onTaskClick(id)

        verify(observer, times(1)).onChanged(id)
    }

    @Test
    fun `loading event triggered when loading resource observed`() {
        val observer: Observer<Boolean> = mock()
        viewModel.loadingEvent.observeForever(observer)

        viewModel.tasksObserver.onNext(Resource.Loading())

        verify(observer, times(1)).onChanged(true)
    }

    @Test
    fun `error event triggered when failure resource observed`() {
        val loadingObserver: Observer<Boolean> = mock()
        viewModel.loadingEvent.observeForever(loadingObserver)
        val errorObserver: Observer<Throwable> = mock()
        viewModel.errorEvent.observeForever(errorObserver)

        val error = mock<Throwable>()
        viewModel.tasksObserver.onNext(Resource.Failure(error))

        verify(loadingObserver, times(1)).onChanged(false)
        verify(errorObserver, times(1)).onChanged(error)
    }

    @Test
    fun `tasks event triggered when success resource observed`() {
        val loadingObserver: Observer<Boolean> = mock()
        viewModel.loadingEvent.observeForever(loadingObserver)
        val tasksObserver: Observer<List<Task>> = mock()
        viewModel.tasks.observeForever(tasksObserver)

        val data = mock<List<Task>>()
        viewModel.tasksObserver.onNext(Resource.Success(data))

        verify(loadingObserver, times(1)).onChanged(false)
        verify(tasksObserver, times(1)).onChanged(data)
    }

    @Test
    fun `error event triggered when uncaught error thrown`() {
        val loadingObserver: Observer<Boolean> = mock()
        viewModel.loadingEvent.observeForever(loadingObserver)
        val errorObserver: Observer<Throwable> = mock()
        viewModel.errorEvent.observeForever(errorObserver)

        val error = mock<Throwable>()
        viewModel.tasksObserver.onError(error)

        verify(loadingObserver, times(1)).onChanged(false)
        verify(errorObserver, times(1)).onChanged(error)
    }

    @Test
    fun `loading event triggered when tasks subject completes`() {
        val observer: Observer<Boolean> = mock()
        viewModel.loadingEvent.observeForever(observer)

        viewModel.tasksObserver.onComplete()

        verify(observer, times(1)).onChanged(false)
    }
}