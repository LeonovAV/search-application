package com.anleonov.indexer.executor

import org.slf4j.LoggerFactory
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService

object ExecutorsProvider {

    private val logger = LoggerFactory.getLogger(ExecutorsProvider::class.java)

    private var numberOfThreads = 1
    private const val numberOfThreadsForScheduledExecutor = 2

    init {
        val availableProcessors = Runtime.getRuntime().availableProcessors()
        val remainingCores = availableProcessors / 3
        if (remainingCores > 1) numberOfThreads = remainingCores
    }

    val executorService: ExecutorService by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        logger.info("Initialize executor service with $numberOfThreads threads")
        Executors.newFixedThreadPool(numberOfThreads)
    }

    val scheduledExecutorService: ScheduledExecutorService by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        logger.info("Initialize scheduled executor service with $numberOfThreadsForScheduledExecutor threads")
        Executors.newScheduledThreadPool(numberOfThreadsForScheduledExecutor)
    }

}
