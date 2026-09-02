package org.openntf.drapi.internal.test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class ConcurrentUtils {

    public static void runMultipleThreadsAtTheSameTime(int numberOfThreads, Runnable task) {

        CyclicBarrier barrier = new CyclicBarrier(numberOfThreads);
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        List<Future<?>> futures = new ArrayList<>();

        for (int i = 0; i < numberOfThreads; i++) {
            futures.add(executorService.submit(() -> {
                try {
                    barrier.await(); // Wait for all threads to be ready
                    task.run(); // Execute the task
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // Restore interrupted status
                } catch (BrokenBarrierException e) {
                    throw new RuntimeException(e);
                }
            }));
        }

        executorService.shutdown();

        futures.forEach(future -> {
            try {
                future.get(); // Wait for each task to complete
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

    }

}
