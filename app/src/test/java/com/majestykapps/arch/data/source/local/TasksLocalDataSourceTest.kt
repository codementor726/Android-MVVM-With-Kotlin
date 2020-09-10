package com.majestykapps.arch.data.source.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.majestykapps.arch.data.common.Resource
import com.majestykapps.arch.domain.entity.Task
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Observer
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

class TasksLocalDataSourceTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Mock
    private lateinit var dao: TasksDao

    @Captor
    private lateinit var listCaptor: ArgumentCaptor<Resource<List<Task>>>

    @Captor
    private lateinit var taskCaptor: ArgumentCaptor<Resource<Task>>

    private val dataSource: TasksLocalDataSource by lazy { TasksLocalDataSource.getInstance(dao) }

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
    }

    @After
    fun clearMocks() {
        // Ensures inline Kotlin mocks do not leak
        Mockito.framework().clearInlineMocks()
        TasksLocalDataSource.destroy()
    }

    @Test
    fun `get all tasks`() {
        val tasks = mock<List<Task>>()
        whenever(dao.getTasks()).thenReturn(Observable.just(tasks))

        val observer: Observer<Resource<List<Task>>> = mock()
        dataSource.getTasks().subscribe(observer)

        verify(observer, times(1)).onNext(listCaptor.capture())
        assertTrue(listCaptor.value is Resource.Success)
        assertEquals(listCaptor.value.data, tasks)
    }

    @Test
    fun `get a task`() {
        val taskId = "a"
        val task = mock<Task> {
            on { id } doReturn taskId
        }
        whenever(dao.getTaskById(taskId)).thenReturn(Maybe.just(task))

        val observer: Observer<Resource<Task>> = mock()
        dataSource.getTask(taskId).subscribe(observer)

        verify(observer, times(1)).onNext(taskCaptor.capture())
        assertTrue(taskCaptor.value is Resource.Success)
        assertEquals(taskCaptor.value.data, task)
    }
}