package bgu.spl.app;

import java.util.concurrent.CountDownLatch;

public class StopLatchedThread extends Thread {
  private final CountDownLatch stopLatch;
  
  public StopLatchedThread(CountDownLatch stopLatch) {
    this.stopLatch = stopLatch;
  }
  public void run() {
    try {
      // perform interesting task
    } finally {
      stopLatch.countDown();
    }
  }
}