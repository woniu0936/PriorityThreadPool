package com.woniu.prioritythreadpool.core;

import java.util.concurrent.Callable;

/**
 * @author woniu
 * @title PriorityCallable
 * @description
 * @since 2018/2/3 下午3:06
 */
public class PriorityCallable<V> implements Callable<V> {

    private final Priority priority;

    public PriorityCallable(Priority priority) {
        this.priority = priority;
    }

    @Override
    public V call() throws Exception {
        return null;
    }

    public Priority getPriority() {
        return priority;
    }
    
}
