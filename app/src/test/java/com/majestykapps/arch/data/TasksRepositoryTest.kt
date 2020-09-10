package com.majestykapps.arch.data

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.majestykapps.arch.common.TestSchedulerProvider
import com.majestykapps.arch.data.common.Resource
import com.majestykapps.arch.data.repository.TasksRepositoryImpl
import com.majestykapps.arch.data.source.local.TasksLocalDataSource
import com.majestykapps.arch.data.source.remote.TasksRemoteDataSource
import com.majestykapps.arch.domain.entity.Task
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Observer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations

class TasksRepositoryTest {

    /**
     * Runs Arch Components on a synchronous executor
     */
    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Mock
    private lateinit var localDataSource: TasksLocalDataSource

    @Mock
    private lateinit var remoteDataSource: TasksRemoteDataSource

    /**
     * Runs RxJava synchronously
     */
    private val schedulerProvider = TestSchedulerProvider()

    /**
     * Receives emissions from [TasksRepositoryImpl.tasksSubject]
     */
    private val tasksObserver: Observer<Resource<List<Task>>> = mock()

    private lateinit var repository: TasksRepositoryImpl

    @Captor
    private lateinit var resourceDataCaptor: ArgumentCaptor<Resource<List<Task>>>

    @Before
    fun setup() {
        // Allows us to use @Mock annotations
        MockitoAnnotations.initMocks(this)

        repository = TasksRepositoryImpl.getInstance(
            localDataSource,
            remoteDataSource,
            schedulerProvider
        ).apply {
            subscribe(tasksObserver)
        }
    }

    @After
    fun clearMocks() {
        // Ensures inline Kotlin mocks do not leak
        Mockito.framework().clearInlineMocks()
        TasksRepositoryImpl.destroy()
    }

    @Test
    fun `task subject subscribed`() {
        verify(tasksObserver, times(1)).onSubscribe(any())
    }

    @Test
    fun `task subscription observes task emission`() {
        val resource: Resource<List<Task>> = mock()
        repository.tasksSubject.onNext(resource)
        verify(tasksObserver, times(1)).onNext(resource)
    }

    @Test
    fun `cache is cleared and marked dirty on refresh`() {
        repository.apply {
            cachedTasks["a"] = mock()
            isCacheDirty = false
        }

        repository.refresh()

        assertTrue(repository.cachedTasks.isEmpty())
        assertTrue(repository.isCacheDirty)
    }

    @Test
    fun `cached tasks returned when cache is not empty or dirty`() {
        val task = Task("a", "test", "task")
        repository.apply {
            isCacheDirty = false
            cachedTasks["a"] = task
        }

        repository.loadTasks()

        verify(tasksObserver, times(1)).onNext(resourceDataCaptor.capture())
        assertTrue(resourceDataCaptor.value is Resource.Success)
        assertEquals(ArrayList(repository.cachedTasks.values), resourceDataCaptor.value.data)
    }

    @Test
    fun `cached tasks not returned when cache is dirty`() {
        whenever(localDataSource.getTasks()).thenReturn(Observable.never<Resource<List<Task>>>())
        whenever(remoteDataSource.getTasks()).thenReturn(Observable.never<Resource<List<Task>>>())

        repository.loadTasks()

        verify(tasksObserver, never()).onNext(any())
    }

    @Test
    fun `remote tasks returned when cache is dirty`() {
        whenever(localDataSource.getTasks()).thenReturn(Observable.never<Resource<List<Task>>>())
        val resource: Resource<List<Task>> = mock()
        whenever(remoteDataSource.getTasks()).thenReturn(Observable.just(resource))

        repository.loadTasks()

        verify(tasksObserver, times(1)).onNext(resource)
    }

    @Test
    fun `local tasks returned when cache is dirty and remote call fails`() {
        whenever(remoteDataSource.getTasks()).thenReturn(Observable.error(RuntimeException()))
        val resource: Resource<List<Task>> = mock()
        whenever(localDataSource.getTasks()).thenReturn(Observable.just(resource))

        repository.loadTasks()

        verify(tasksObserver, times(1)).onNext(resource)
    }

    @Test
    fun `local tasks returned when cache is not dirty but is empty`() {
        repository.isCacheDirty = false
        val resource: Resource<List<Task>> = mock()
        whenever(localDataSource.getTasks()).thenReturn(Observable.just(resource))

        repository.loadTasks()

        verify(tasksObserver, times(1)).onNext(resource)
    }

    @Test
    fun `remote tasks are cached when successfully retrieved`() {
        whenever(localDataSource.getTasks()).thenReturn(Observable.never<Resource<List<Task>>>())
        val task = mock<Task> {
            on { id } doReturn "a"
        }
        val data = listOf(task)
        val resource = Resource.Success(data)
        whenever(remoteDataSource.getTasks()).thenReturn(Observable.just(resource))

        repository.loadTasks()

        assertEquals(repository.cachedTasks["a"], task)
    }

    @Test
    fun `remote tasks are saved to db when successfully retrieved`() {
        whenever(localDataSource.getTasks()).thenReturn(Observable.never<Resource<List<Task>>>())
        val data = listOf(mock<Task>())
        val resource = Resource.Success(data)
        whenever(remoteDataSource.getTasks()).thenReturn(Observable.just(resource))

        repository.loadTasks()

        verify(localDataSource, times(1)).saveTasks(data)
    }

    @Test
    fun `cache is marked clean when remote tasks are cached`() {
        whenever(localDataSource.getTasks()).thenReturn(Observable.never<Resource<List<Task>>>())
        whenever(localDataSource.saveTasks(any())).thenReturn(Completable.never())
        val data = listOf(mock<Task>())
        val resource = Resource.Success(data)
        whenever(remoteDataSource.getTasks()).thenReturn(Observable.just(resource))

        repository.loadTasks()

        assertFalse(repository.isCacheDirty)
    }

    @Test
    fun `local tasks are cached when successfully retrieved`() {
        repository.isCacheDirty = false
        val task = mock<Task> {
            on { id } doReturn "a"
        }
        val data = listOf(task)
        val resource = Resource.Success(data)
        whenever(localDataSource.getTasks()).thenReturn(Observable.just(resource))

        repository.loadTasks()

        assertEquals(repository.cachedTasks["a"], task)
    }
}
