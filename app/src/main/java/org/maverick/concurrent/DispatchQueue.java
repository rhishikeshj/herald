package org.maverick.concurrent;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Concurrency util class which tries to mimic the functionality
 * of the dispatchQueues found in iOS
 * <p/>
 * Serial dispatch queues will take runnables and run them one after the other in FIFO order.
 * Concurrent dispatch queues will take runnables and run them concurrently in a ThreadPool.
 */
public class DispatchQueue {

  private ArrayList<Future> tasks;
  private ArrayList<Thread> afterThreads;
  private boolean isConcurrent;
  private ExecutorService threadPoolExecutor;

  public DispatchQueue(boolean isConcurrent) {
    this.isConcurrent = isConcurrent;
    this.tasks = new ArrayList<>();
    this.afterThreads = new ArrayList<>();
    if (this.isConcurrent) {
      this.threadPoolExecutor = Executors.newCachedThreadPool();
    } else {
      this.threadPoolExecutor = Executors.newSingleThreadExecutor();
    }
  }

  synchronized public void dispatchSync(Runnable runnable) {
    Future taskFuture = this.threadPoolExecutor.submit(runnable);
    try {
      taskFuture.get();
    } catch (InterruptedException e) {
    } catch (ExecutionException e) {
    }
  }

  private void trackTask(Future task) {
    synchronized (this.tasks) {
      tasks.add(task);
    }
  }

  synchronized public void dispatchAsync(Runnable runnable) {
    this.trackTask(this.threadPoolExecutor.submit(runnable));
  }

  synchronized public void dispatchAfter(final Runnable runnable, final long interval) {
    final DispatchQueue queue = this;
    Thread triggerThread = new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          Thread.sleep(interval);
          queue.dispatchAsync(runnable);
        } catch (InterruptedException e) {
        }
      }
    });
    triggerThread.start();
    this.afterThreads.add(triggerThread);
  }

  public void join() {
    try {
      synchronized (this.afterThreads) {
        for (Thread t : this.afterThreads) {
          t.join();
        }
      }
      synchronized (this.tasks) {
        for (Future task : this.tasks) {
          task.get();
        }
        this.tasks.clear();
      }
    } catch (InterruptedException e) {
    } catch (ExecutionException e) {
    }
  }
}
