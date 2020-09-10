package com.majestykapps.arch.data.source.local

import androidx.annotation.VisibleForTesting
import com.majestykapps.arch.data.common.Resource
import com.majestykapps.arch.data.source.TasksDataSource
import com.majestykapps.arch.domain.entity.Task
import io.reactivex.Completable
import io.reactivex.Observable

class TasksLocalDataSource private constructor(
    private val tasksDao: TasksDao
) : TasksDataSource {

    override fun getTasks(): Observable<Resource<List<Task>>> = tasksDao.getTasks()
        .flatMap { Observable.just(Resource.Success(it)) }

    override fun getTask(taskId: String): Observable<Resource<Task>> = tasksDao.getTaskById(taskId)
        .flatMapObservable { Observable.just(Resource.Success(it)) }

    override fun saveTask(task: Task): Completable = tasksDao.insertTask(task)

    override fun saveTasks(tasks: List<Task>) = Completable.fromAction {
        tasksDao.insertTasks(tasks)
    }

    override fun deleteTask(taskId: String): Completable = tasksDao.deleteTaskById(taskId)

    companion object {
        private var INSTANCE: TasksLocalDataSource? = null

        fun getInstance(
            tasksDao: TasksDao
        ): TasksLocalDataSource = INSTANCE ?: synchronized(this) {
            INSTANCE ?: TasksLocalDataSource(tasksDao).also { INSTANCE = it }
        }

        @VisibleForTesting
        fun destroy() {
            INSTANCE = null
        }
    }
}