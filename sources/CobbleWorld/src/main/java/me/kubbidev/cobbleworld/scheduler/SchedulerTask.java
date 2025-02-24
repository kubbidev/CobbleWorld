package me.kubbidev.cobbleworld.scheduler;

/**
 * Represents a scheduled task
 */
public interface SchedulerTask {

    /**
     * Cancels the task.
     */
    void cancel();
}