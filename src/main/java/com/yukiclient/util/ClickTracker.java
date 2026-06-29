package com.yukiclient.util;

import java.util.ArrayDeque;

/**
 * Reusable rolling click-rate counter.
 *
 * <p>Tracks how many rising edges (a press following a release) happened within
 * the last second. Both {@code CpsModule} and {@code KeystrokesModule} share this
 * implementation instead of each duplicating the same bookkeeping.</p>
 *
 * <p>Call {@link #update(boolean)} once per tick with the current button state,
 * then read {@link #getCps()} for the clicks-per-second value.</p>
 */
public final class ClickTracker {

    /** Rolling window length in milliseconds (one second). */
    private static final long WINDOW_MS = 1000L;

    private final ArrayDeque<Long> clicks = new ArrayDeque<Long>();
    private boolean wasDown = false;

    /**
     * Records a click on the rising edge and prunes expired entries.
     *
     * @param down whether the tracked button is currently held down.
     */
    public void update(boolean down) {
        long now = System.currentTimeMillis();

        if (down && !wasDown) {
            clicks.addLast(now);
        }
        wasDown = down;

        // Remove clicks older than the window. ArrayDeque#pollFirst is O(1).
        while (!clicks.isEmpty() && now - clicks.peekFirst() > WINDOW_MS) {
            clicks.pollFirst();
        }
    }

    /**
     * @return the number of clicks recorded within the last second.
     */
    public int getCps() {
        return clicks.size();
    }

    /**
     * Clears all tracked state.
     */
    public void reset() {
        clicks.clear();
        wasDown = false;
    }
}
