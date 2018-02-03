package com.woniu.prioritythreadpool;

import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.woniu.prioritythreadpool.core.Core;
import com.woniu.prioritythreadpool.core.Priority;
import com.woniu.prioritythreadpool.core.PriorityCallable;

import java.util.concurrent.Future;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        for (int i = 0; i < 30; i++) {
            if (i % 5 == 0) {
                run(Priority.HIGH, i);
            } else {
                run(Priority.MEDIUM, i);
            }
        }
    }

    private void run(Priority priority, final int i) {
//        Core.getInstance().getExecutorSupplier().forNetworkTasks().submit(new PriorityRunnable(priority) {
//            @Override
//            public void run() {
//                SystemClock.sleep(100);
//                Log.d(TAG, "执行线程： " + i);
//            }
//        });
        Future<Void> future = Core.getInstance().getExecutorSupplier().forImmediateNetworkTasks().submit(new PriorityCallable<Void>(priority) {
            @Override
            public Void call() throws Exception {
                SystemClock.sleep(1000);
                Log.d(TAG, "执行线程： " + i);
                return null;
            }
        });
    }
}
