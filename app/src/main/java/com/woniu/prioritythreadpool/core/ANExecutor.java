/*
 *    Copyright (C) 2016 Amit Shekhar
 *    Copyright (C) 2011 Android Open Source Project
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.woniu.prioritythreadpool.core;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.telephony.TelephonyManager;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by amitshekhar on 22/03/16.
 */
public class ANExecutor extends ThreadPoolExecutor {

    private static final int DEFAULT_THREAD_COUNT = 3;

    ANExecutor(int maxNumThreads, ThreadFactory threadFactory) {
        super(maxNumThreads, maxNumThreads, 0, TimeUnit.MILLISECONDS,
                new PriorityBlockingQueue<Runnable>(), threadFactory);
    }


    void adjustThreadCount(NetworkInfo info) {
        if (info == null || !info.isConnectedOrConnecting()) {
            setThreadCount(DEFAULT_THREAD_COUNT);
            return;
        }
        switch (info.getType()) {
            case ConnectivityManager.TYPE_WIFI:
            case ConnectivityManager.TYPE_WIMAX:
            case ConnectivityManager.TYPE_ETHERNET:
                setThreadCount(4);
                break;
            case ConnectivityManager.TYPE_MOBILE:
                switch (info.getSubtype()) {
                    case TelephonyManager.NETWORK_TYPE_LTE:  // 4G
                    case TelephonyManager.NETWORK_TYPE_HSPAP:
                    case TelephonyManager.NETWORK_TYPE_EHRPD:
                        setThreadCount(3);
                        break;
                    case TelephonyManager.NETWORK_TYPE_UMTS: // 3G
                    case TelephonyManager.NETWORK_TYPE_CDMA:
                    case TelephonyManager.NETWORK_TYPE_EVDO_0:
                    case TelephonyManager.NETWORK_TYPE_EVDO_A:
                    case TelephonyManager.NETWORK_TYPE_EVDO_B:
                        setThreadCount(2);
                        break;
                    case TelephonyManager.NETWORK_TYPE_GPRS: // 2G
                    case TelephonyManager.NETWORK_TYPE_EDGE:
                        setThreadCount(1);
                        break;
                    default:
                        setThreadCount(DEFAULT_THREAD_COUNT);
                }
                break;
            default:
                setThreadCount(DEFAULT_THREAD_COUNT);
        }
    }

    private void setThreadCount(int threadCount) {
        setCorePoolSize(threadCount);
        setMaximumPoolSize(threadCount);
    }

    @Override
    public Future<?> submit(Runnable task) {
        AndroidNetworkingFutureTask futureTask = new AndroidNetworkingFutureTask((PriorityRunnable) task);
        execute(futureTask);
        return futureTask;
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        PriorityFutureTask futureTask = new PriorityFutureTask((PriorityCallable) task);
        execute(futureTask);
        return futureTask;
    }

    private static final class AndroidNetworkingFutureTask extends FutureTask<PriorityRunnable>
            implements Comparable<AndroidNetworkingFutureTask> {
        private final PriorityRunnable hunter;

        public AndroidNetworkingFutureTask(PriorityRunnable hunter) {
            super(hunter, null);
            this.hunter = hunter;
        }

        @Override
        public int compareTo(AndroidNetworkingFutureTask other) {
            Priority p1 = hunter.getPriority();
            Priority p2 = other.hunter.getPriority();
            return p2.ordinal() - p1.ordinal();
        }
    }

    public static final class PriorityFutureTask<T> extends FutureTask<T> implements Comparable<PriorityFutureTask<T>> {

        private final PriorityCallable<T> hunter;

        public PriorityFutureTask(@NonNull Callable<T> callable) {
            super(callable);
            this.hunter = (PriorityCallable<T>) callable;
        }

        @Override
        public int compareTo(@NonNull PriorityFutureTask<T> other) {
            Priority p1 = hunter.getPriority();
            Priority p2 = other.hunter.getPriority();
            return p2.ordinal() - p1.ordinal();
        }
    }

    @Override
    protected <T> RunnableFuture<T> newTaskFor(Callable<T> callable) {
        return new PriorityFutureTask<>(callable);
    }
}
