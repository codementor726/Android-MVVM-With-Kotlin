package com.majestykapps.arch.presentation.taskdetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.majestykapps.arch.data.common.Resource.Failure
import com.majestykapps.arch.data.common.Resource.Loading
import com.majestykapps.arch.data.common.Resource.Success
import com.majestykapps.arch.domain.entity.Task
import com.majestykapps.arch.domain.usecase.GetTaskUseCase
import com.majestykapps.arch.presentation.common.BaseViewModel
import com.majestykapps.arch.util.SingleLiveEvent

class TaskDetailViewModel(
    private val getTaskUseCase: GetTaskUseCase
) : BaseViewModel() {

    val loadingEvent = SingleLiveEvent<Void>()
    val errorEvent = SingleLiveEvent<Throwable>()

    private val task = MutableLiveData<Task>()

    val title: LiveData<String> = Transformations.switchMap(task) {
        MutableLiveData<String>(it.title)
    }

    val description: LiveData<String> = Transformations.switchMap(task) {
        MutableLiveData<String>(it.description)
    }

    fun getTask(id: String, forceReload: Boolean = false) {
        if (forceReload) getTaskUseCase

        val disposable = getTaskUseCase.getTask(id)
            .subscribe({ resource ->
                when (resource) {
                    is Loading -> loadingEvent.call()
                    is Failure -> errorEvent.value = resource.error
                    is Success -> task.value = resource.data
                }
            }, { throwable ->
                errorEvent.value = throwable
            })
        disposables.add(disposable)
    }
}