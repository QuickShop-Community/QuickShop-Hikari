package com.ghostchu.quickshop.common.util;

import java.util.Objects;

/**
 * Simple Timer for QuickShop to calc time passed (timestamp based)
 *
 * @author Ghost_chu
 */
public class Timer {

  private long startTime;
  private long passedTime;
  private boolean isPaused = false;

  /**
   * Create a empty timer, use setTimer to start
   */
  public Timer() {

  }

  /**
   * Create a empty timer, auto start if autoStart is true
   *
   * @param autoStart Auto set the timer
   */
  public Timer(final boolean autoStart) {

    if(autoStart) {
      start();
    }
  }

  /**
   * Create a empty time, use the param to init the startTime.
   *
   * @param startTime New startTime
   */
  public Timer(final long startTime) {

    this.startTime = startTime;
  }

  /**
   * Start the timer. Time Unit: ms
   */
  public void start() {

    this.startTime = System.currentTimeMillis();
    isPaused = false;
  }

  /**
   * Return how long time running after a specified time. THIS NOT WILL DESTORY AND STOP THE TIMER
   *
   * @param atTime The specified time
   *
   * @return time
   */
  public long getPassedTimeOffsetFrom(final long atTime) {

    return (atTime - startTime) + passedTime;
  }

  /**
   * Pause the timer. Time Unit: ms
   */
  public void pause() {

    this.passedTime = getPassedTime();
    isPaused = true;
  }

  /**
   * Return how long time running when timer set. THIS NOT WILL DESTORY AND STOP THE TIMER
   *
   * @return time
   */
  public long getPassedTime() {

    if(isPaused) {
      return passedTime;
    } else {
      return System.currentTimeMillis() - startTime;
    }
  }

  /**
   * Resume the timer. Time Unit: ms
   */
  public void resume() {

    if(isPaused) {
      this.startTime = System.currentTimeMillis() - passedTime;
      passedTime = 0;
      isPaused = false;
    }
  }

  /**
   * Return how long time running when timer set and destory the timer.
   *
   * @return time
   */
  public long stopAndGetTimePassed() {

    final long time = getPassedTime();
    startTime = 0;
    return time;
  }

  @Override
  public boolean equals(final Object o) {

    if(o == this) return true;
    if(!(o instanceof Timer)) return false;
    final Timer other = (Timer)o;
    return this.startTime == other.startTime
           && this.isPaused == other.isPaused;
  }

  @Override
  public int hashCode() {

    return Objects.hash(this.startTime, this.isPaused);
  }

  @Override
  public String toString() {

    return "Timer(startTime=" + this.startTime + ", passedTime=" + this.getPassedTime() + ", isPaused=" + this.isPaused + ")";
  }
}
