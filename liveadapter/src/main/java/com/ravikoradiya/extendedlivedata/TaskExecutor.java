package com.ravikoradiya.extendedlivedata;

import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;

/**
 * A task executor that can divide tasks into logical groups.
 * <p>
 * It holds a collection a executors for each group of task.
 * <p>
 * TODO: Don't use this from outside, we don't know what the API will look like yet.
 * @hide
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
public abstract class TaskExecutor {
    /**
     * Executes the given task in the disk IO thread pool.
     *
     * @param runnable The runnable to run in the disk IO thread pool.
     */
    public abstract void executeOnDiskIO(@NonNull Runnable runnable);

    /**
     * Posts the given task to the main thread.
     *
     * @param runnable The runnable to run on the main thread.
     */
    public abstract void postToMainThread(@NonNull Runnable runnable);

    /**
     * Executes the given task on the main thread.
     * <p>
     * If the current thread is a main thread, immediately runs the given runnable.
     *
     * @param runnable The runnable to run on the main thread.
     */
    public void executeOnMainThread(@NonNull Runnable runnable) {
        if (isMainThread()) {
            runnable.run();
        } else {
            postToMainThread(runnable);
        }
    }

    /**
     * Returns true if the current thread is the main thread, false otherwise.
     *
     * @return true if we are on the main thread, false otherwise.
     */
    public abstract boolean isMainThread();
}