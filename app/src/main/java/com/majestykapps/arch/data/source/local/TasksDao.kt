package com.majestykapps.arch.data.source.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.majestykapps.arch.domain.entity.Task
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Observable

/**
 * Data Access Object for the tasks table.
 *
 * Using abstract class (instead of interface) to allow for @Transaction functions
 */
@Dao
abstract class TasksDao {

    /**
     * Select all tasks from the tasks table.
     */
    @Query("SELECT * FROM Tasks")
    abstract fun getTasks(): Observable<List<Task>>

    /**
     * Select a task by id.
     *
     * @param taskId the task id.
     */
    @Query("SELECT * FROM Tasks WHERE id = :taskId")
    abstract fun getTaskById(taskId: String): Maybe<Task>

    /**
     * Insert a task in the database. If the task already exists, replace it.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertTask(task: Task): Completable

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertTaskSync(task: Task)

    /**
     * Delete a task by id.
     *
     * @param taskId the task id.
     */
    @Query("DELETE FROM Tasks WHERE id = :taskId")
    abstract fun deleteTaskById(taskId: String): Completable

    /**
     * Bulk insert in a transaction
     */
    @Transaction
    open fun insertTasks(tasks: List<Task>) {
        tasks.forEach { insertTaskSync(it) }
    }
}