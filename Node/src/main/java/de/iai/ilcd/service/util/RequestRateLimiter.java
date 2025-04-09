package de.iai.ilcd.service.util;

import java.time.Duration;
import java.time.Instant;

/**
 * Given a time frame size (in seconds) and a number of events that shouldn't be
 * exceeded within such a frame, this class takes an appropriate nap, if
 * necessary.<br>
 * <br>
 * Note: It averages the total number of events during the total time spent to
 * estimate how many events have already occured within (!) the current frame
 * when a maximum number is of events is reached.
 */
public class RequestRateLimiter {

    private static final long STANDARD_FRAME_SIZE = 60;

    private final long frameSize;

    private final long requestLimit;

    private Instant instantOfCreation;

    private long dataSetCounter = 0;

    private long totalCount = 0;

    private long framesPassed = 0;

    private double averagedFrequency = 0;

    public RequestRateLimiter(long requestLimit, long frameSize) {
        this.instantOfCreation = Instant.now();
        this.requestLimit = requestLimit;
        this.frameSize = frameSize;
    }

    public RequestRateLimiter(long requestLimit) {
        this(requestLimit, STANDARD_FRAME_SIZE);
    }

    public void check() {
        if (dataSetCounter >= requestLimit - 1)
            ponderSleep();
        if (this.isNewFrame())
            reset();
        registerEvent();
    }

    private void ponderSleep() {
        if (!this.isNewFrame())
            try {
                Thread.sleep(getMilliFrame());
            } catch (InterruptedException e) {
                ponderSleep();
            }
    }

    private void reset() {
        try {
            dataSetCounter = (long) ((double) getFrame() * averagedFrequency) + 1;
        } catch (ArithmeticException ae) {
            reset();
        }
    }

    private long getFrame() {
        long passed = timeElapsed() % frameSize;
        return frameSize - passed;
    }

    private long getMilliFrame() {
        return getFrame() * 1000;
    }

    private boolean isNewFrame() {
        long framesPassedThen = framesPassed;
        framesPassed = (long) timeElapsed() / frameSize;
        return framesPassed - framesPassedThen > 0;
    }

    private long timeElapsed() {
        Instant timeNow = Instant.now();
        return Duration.between(instantOfCreation, timeNow).getSeconds();
    }

    private void registerEvent() {
        this.totalCount++;
        this.dataSetCounter++;
        this.averagedFrequency = (double) totalCount / (double) timeElapsed();
    }
}
