package com.majestykapps.arch.data.source

import com.majestykapps.arch.data.common.Resource
import com.majestykapps.arch.domain.entity.Task
import io.reactivex.Completable
import io.reactivex.Observable

/**
 * Main entry point for accessing tasks data.
 */
interface TasksDataSource {

    fun getTasks(): Observable<Resource<List<Task>>>

    fun getTask(taskId: String): Observable<Resource<Task>>

    fun saveTask(task: Task): Completable

    fun saveTasks(tasks: List<Task>): Completable

    fun deleteTask(taskId: String): Completable
}