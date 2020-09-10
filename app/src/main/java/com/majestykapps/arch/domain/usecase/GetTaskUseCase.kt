package com.majestykapps.arch.domain.usecase

import com.majestykapps.arch.data.common.Resource
import com.majestykapps.arch.domain.entity.Task
import com.majestykapps.arch.domain.repository.TasksRepository
import io.reactivex.Observable

interface GetTaskUseCase {
    fun getTask(id: String): Observable<Resource<Task>>
    fun refresh()
}

class GetTask(
    private val repository: TasksRepository
) : GetTaskUseCase {
    override fun getTask(id: String): Observable<Resource<Task>> = repository.getTask(id)
    override fun refresh() = repository.refresh()
}