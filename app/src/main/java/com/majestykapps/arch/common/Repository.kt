package com.majestykapps.arch.common

/**
 * Repository provide data from 1 or more data sources and implement an in-memory cache.
 */
interface Repository {
    /**
     * Clear the cache
     */
    fun refresh()
}