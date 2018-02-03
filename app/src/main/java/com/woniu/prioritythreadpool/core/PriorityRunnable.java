package com.woniu.prioritythreadpool.core;

/**
 * @author woniu
 * @title PriorityRunnable
 * @description
 * @since 2018/2/3 下午2:26
 */
public class PriorityRunnable implements Runnable {

    private final Priority priority;

    public PriorityRunnable(Priority priority) {
        this.priority = priority;
    }

    @Override
    public void run() {

    }

    public Priority getPriority() {
        return priority;
    }

}
