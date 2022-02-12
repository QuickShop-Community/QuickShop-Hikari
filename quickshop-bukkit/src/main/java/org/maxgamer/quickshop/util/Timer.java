/*
 * This file is a part of project QuickShop, the name is Timer.java
 *  Copyright (C) PotatoCraft Studio and contributors
 *
 *  This program is free software: you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License as published by the
 *  Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *  for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.maxgamer.quickshop.util;

import lombok.EqualsAndHashCode;
import lombok.ToString;


/**
 * Simple Timer for QuickShop to calc time passed (timestamp based)
 *
 * @author Ghost_chu
 */
@EqualsAndHashCode
@ToString
public class Timer {
    private long startTime;
    @EqualsAndHashCode.Exclude
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
    public Timer(boolean autoStart) {
        if (autoStart) {
            start();
        }
    }

    /**
     * Create a empty time, use the param to init the startTime.
     *
     * @param startTime New startTime
     */
    public Timer(long startTime) {
        this.startTime = startTime;
    }

    /**
     * Return how long time running when timer set and destory the timer.
     *
     * @return time
     */
    public long stopAndGetTimePassed() {
        long time = getPassedTime();
        startTime = 0;
        return time;
    }

    /**
     * Return how long time running after a specified time. THIS NOT WILL DESTORY AND STOP THE TIMER
     *
     * @param atTime The specified time
     * @return time
     */
    public long getPassedTimeOffsetFrom(long atTime) {
        return (atTime - startTime) + passedTime;
    }

    /**
     * Start the timer. Time Unit: ms
     */
    public void start() {
        this.startTime = System.currentTimeMillis();
        isPaused = false;
    }

    /**
     * Pause the timer. Time Unit: ms
     */
    public void pause() {
        this.passedTime = getPassedTime();
        isPaused = true;
    }

    /**
     * Resume the timer. Time Unit: ms
     */
    public void resume() {
        if (isPaused) {
            this.startTime = System.currentTimeMillis() - passedTime;
            passedTime = 0;
            isPaused = false;
        }
    }

    /**
     * Return how long time running when timer set. THIS NOT WILL DESTORY AND STOP THE TIMER
     *
     * @return time
     */
    public long getPassedTime() {
        if (isPaused) {
            return passedTime;
        } else {
            return System.currentTimeMillis() - startTime;
        }
    }

}
