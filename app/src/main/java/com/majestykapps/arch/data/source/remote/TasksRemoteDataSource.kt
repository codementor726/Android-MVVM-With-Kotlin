package com.majestykapps.arch.data.source.remote

import androidx.annotation.VisibleForTesting
import com.majestykapps.arch.data.api.ApiResponse
import com.majestykapps.arch.data.api.TasksApi
import com.majestykapps.arch.data.api.TasksApiService
import com.majestykapps.arch.data.common.ApiException
import com.majestykapps.arch.data.common.Resource
import com.majestykapps.arch.data.source.TasksDataSource
import com.majestykapps.arch.domain.entity.Task
import io.reactivex.Completable
import io.reactivex.Observable
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TasksRemoteDataSource private constructor(
    private val api: TasksApiService
) : TasksDataSource {

    override fun getTasks(): Observable<Resource<List<Task>>> = Observable.create { emitter ->
        // First notify observers that loading has begun
        emitter.onNext(Resource.Loading())

        val apiCall = api.getTasks()

        // Cancel the network call if disposed before it's finished
        emitter.setCancellable { apiCall.cancel() }

        // Queue the API network call
        apiCall.enqueue(object : Callback<ApiResponse<Task>> {
            override fun onFailure(call: Call<ApiResponse<Task>?>, t: Throwable) {
                emitter.tryOnError(t)
            }

            override fun onResponse(
                call: Call<ApiResponse<Task>?>,
                response: Response<ApiResponse<Task>?>
            ) {
                if (response.isSuccessful) {
                    val taskResponse: ApiResponse<Task>? = response.body()
                    if (taskResponse?.success == true) {
                        emitter.onNext(Resource.Success(taskResponse.records))
                    } else {
                        taskResponse?.message?.let {
                            emitter.tryOnError(ApiException(it))
                        } ?: emitter.tryOnError(RuntimeException("Unknown API error"))
                    }
                } else {
                    emitter.tryOnError(RuntimeException("Unknown API error"))
                }
            }
        })
    }

    override fun getTask(taskId: String): Observable<Resource<Task>> =
        api.getTask(taskId)
            .map<Resource<Task>> { Resource.Success(it) }
            .onErrorReturn { Resource.Failure(it) }

    override fun saveTask(task: Task): Completable {
        return Completable.complete() // TODO
    }

    override fun saveTasks(tasks: List<Task>): Completable {
        return Completable.complete() // TODO
    }

    override fun deleteTask(taskId: String): Completable {
        return Completable.complete() // TODO
    }

    companion object {
        private var INSTANCE: TasksRemoteDataSource? = null

        fun getInstance(
            api: TasksApiService = TasksApi.getInstance().service
        ): TasksRemoteDataSource = INSTANCE ?: synchronized(this) {
            INSTANCE ?: TasksRemoteDataSource(api).also { INSTANCE = it }
        }

        @VisibleForTesting
        fun destroy() {
            INSTANCE = null
        }
    }
}