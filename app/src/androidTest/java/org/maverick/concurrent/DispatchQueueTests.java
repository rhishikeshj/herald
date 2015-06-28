package org.maverick.concurrent;

import android.content.Context;
import android.test.AndroidTestCase;
import android.util.Log;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class DispatchQueueTests extends AndroidTestCase {
  private static final String TAG = "HelpshiftTest";
  private static final int RUN_COUNT = 5;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
  }

  @Override
  public void setContext(Context c) {
    super.setContext(c);
  }

  public void testSerialDispatchSync() {
    DispatchQueue queue = new DispatchQueue(false);
    final AtomicInteger taskCount = new AtomicInteger(RUN_COUNT);
    for (int i = 0; i < RUN_COUNT; i++) {
      queue.dispatchSync(new Runnable() {
        @Override
        public void run() {
          try {
            Thread.sleep(2000);
            taskCount.decrementAndGet();
          } catch (InterruptedException e) {
          }
        }
      });
    }
    assertTrue(taskCount.get() == 0);
  }

  public void testSerialDispatchAsync() {
    DispatchQueue queue = new DispatchQueue(false);
    final AtomicInteger taskCount = new AtomicInteger(RUN_COUNT);
    for (int i = 0; i < RUN_COUNT; i++) {
      queue.dispatchAsync(new Runnable() {
        @Override
        public void run() {
          try {
            Thread.sleep(2000);
            taskCount.decrementAndGet();
          } catch (InterruptedException e) {
          }
        }
      });
    }
    assertTrue(taskCount.get() > 0);
    queue.join();
    assertTrue(taskCount.get() == 0);
  }

  public void testSerialDispatchAfter() {
    DispatchQueue queue = new DispatchQueue(false);
    final AtomicBoolean isDone = new AtomicBoolean(false);

    queue.dispatchAfter(new Runnable() {
      @Override
      public void run() {
        Log.d(TAG, "Task complete");
        isDone.set(true);
      }
    }, 5000);
    queue.join();
    assertTrue(isDone.get());
  }

  public void testConcurrentDispatchAsync() {
    DispatchQueue queue = new DispatchQueue(true);
    final AtomicBoolean firstComplete = new AtomicBoolean(false);
    final AtomicBoolean secondComplete = new AtomicBoolean(false);
    queue.dispatchAsync(new Runnable() {
      @Override
      public void run() {
        try {
          Thread.sleep(3000);
        } catch (InterruptedException e) {
        }
        assertTrue(secondComplete.get());
        firstComplete.set(true);
        Log.d(TAG, "1 Done");
      }
    });
    queue.dispatchAsync(new Runnable() {
      @Override
      public void run() {
        try {
          assertFalse(firstComplete.get());
          Thread.sleep(1000);
        } catch (InterruptedException e) {
        }
        secondComplete.set(true);
        Log.d(TAG, "2 Done");
      }
    });
    queue.join();
  }
}
