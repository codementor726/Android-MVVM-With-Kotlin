package com.majestykapps.arch.common

import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

interface SchedulerProvider {
    val computation: Scheduler
    val io: Scheduler
    val main: Scheduler
    val newThread: Scheduler
    val single: Scheduler
}

class ToDoSchedulerProvider : SchedulerProvider {
    override val computation: Scheduler
        get() = Schedulers.computation()
    override val io: Scheduler
        get() = Schedulers.io()
    override val main: Scheduler
        get() = AndroidSchedulers.mainThread()
    override val newThread: Scheduler
        get() = Schedulers.newThread()
    override val single: Scheduler
        get() = Schedulers.single()
}

class TestSchedulerProvider : SchedulerProvider {
    override val computation: Scheduler
        get() = Schedulers.trampoline()
    override val io: Scheduler
        get() = Schedulers.trampoline()
    override val main: Scheduler
        get() = Schedulers.trampoline()
    override val newThread: Scheduler
        get() = Schedulers.trampoline()
    override val single: Scheduler
        get() = Schedulers.trampoline()
}