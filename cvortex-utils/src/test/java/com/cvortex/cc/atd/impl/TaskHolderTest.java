package com.cvortex.cc.atd.impl;

import java.util.concurrent.ScheduledThreadPoolExecutor;

import org.cvortex.env.ExecutionEnvironment;
import org.cvortex.env.ExecutionEnvironmentReal;
import org.cvortex.env.TimeInterval;
import org.cvortex.events.EventChannel;
import org.cvortex.util.SilentSleep;
import org.cvortex.util.testing.MockUtils;
import org.cvortex.util.testing.WatchDog;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.cvortex.cc.atd.OfferParams;
import com.cvortex.cc.atd.OfferResult;
import com.cvortex.cc.atd.OfferResultEvent;
import com.cvortex.cc.atd.Processor;
import com.cvortex.cc.atd.impl.TaskHolder.State;

import static org.mockito.Mockito.*;

public class TaskHolderTest extends Assert {

    private final OfferParams offerParams = new OfferParams(new TimeInterval(0L), new EventChannelMock(), 0);

    private final DefaultAutomaticTaskDistributor atd = mock(DefaultAutomaticTaskDistributor.class);

    private final ProcessorHolder pHolder = new ProcessorHolder(new Processor() {});

    private final TaskHolder tHolder = new TaskHolder(null, offerParams, atd);

    private final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);

    private final ExecutionEnvironment execEnvironment = new ExecutionEnvironmentReal(executor);

    // Listener fields
    private volatile Processor assignedTo;

    private volatile boolean cancelled;

    private volatile boolean timeout;

    private class EventChannelMock implements EventChannel {

        @Override
        public void publish(Object e) throws InterruptedException {
            assertTrue(e instanceof OfferResultEvent);
            final OfferResultEvent ore = (OfferResultEvent) e;
            if (ore.getResult().equals(OfferResult.ASSIGNED)) {
                assignedTo = ore.getProcessor();
            }
            if (ore.getResult().equals(OfferResult.CANCELLED)) {
                cancelled = true;
            }
            if (ore.getResult().equals(OfferResult.TIMEOUT)) {
                timeout = true;
            }
        }
    }

    @Before
    public void init() {
        when(atd.getExecEnvironment()).thenReturn(execEnvironment);
        assignedTo = null;
        cancelled = false;
        timeout = false;
    }

    @Test
    public void goodOffer() {
        tHolder.offer(pHolder);
        verify(atd, times(1)).offer(tHolder, pHolder);
        verifyState(State.OFFERED);
    }

    @Test
    public void badOfferDueToState() {
        badOfferDueToState(State.OFFERED);
        badOfferDueToState(State.OFFERED_CANCEL);
        badOfferDueToState(State.OFFERED_TIMEOUT);
        badOfferDueToState(State.REMOVE);
    }

    @Test
    public void isAcceptableForDueToBlackList() {
        assertTrue(tHolder.isAcceptableFor(pHolder));
        tHolder.offer(pHolder);
        tHolder.onOfferDone(pHolder, false);
        assertFalse(tHolder.isAcceptableFor(pHolder));
    }

    @Test
    public void onOfferDonePositive() {
        tHolder.offer(pHolder);
        tHolder.onOfferDone(pHolder, true);
        verifyState(State.REMOVE);
        assertFalse(tHolder.canBePlacedToQueue());
        waitAssignedTo();
    }

    @Test
    public void onOfferDoneNegative() {
        tHolder.offer(pHolder);
        tHolder.onOfferDone(pHolder, false);
        verifyState(State.QUEUE);
        assertTrue(tHolder.canBePlacedToQueue());
    }

    @Test
    public void onOfferDonePositiveCancelled() {
        tHolder.offer(pHolder);
        tHolder.cancel();
        tHolder.onOfferDone(pHolder, true);
        verifyState(State.REMOVE);
        assertFalse(tHolder.canBePlacedToQueue());
        waitAssignedTo();
    }

    @Test
    public void onOfferDoneNegativeCancelled() {
        tHolder.offer(pHolder);
        tHolder.cancel();
        tHolder.onOfferDone(pHolder, false);
        verifyState(State.REMOVE);
        assertFalse(tHolder.canBePlacedToQueue());
        waitCancelled();
    }

    @Test
    public void onOfferDonePositiveTimeout() {
        tHolder.offer(pHolder);
        tHolder.onTimeout();
        tHolder.onOfferDone(pHolder, true);
        verifyState(State.REMOVE);
        assertFalse(tHolder.canBePlacedToQueue());
        waitAssignedTo();
    }

    @Test
    public void onOfferDoneNegativeTimeout() {
        tHolder.offer(pHolder);
        tHolder.onTimeout();
        tHolder.onOfferDone(pHolder, false);
        verifyState(State.REMOVE);
        assertFalse(tHolder.canBePlacedToQueue());
        waitTimeout();
    }

    @Test
    public void cancel() {
        tHolder.cancel();
        waitCancelled();
    }

    @Test
    public void timeout() {
        tHolder.onTimeout();
        waitTimeout();
    }

    private void badOfferDueToState(TaskHolder.State state) {
        try {
            MockUtils.setFieldValue(tHolder, "state", state);
            assertFalse(tHolder.isAcceptableFor(pHolder));
            tHolder.offer(pHolder);
            fail("tHolder.offer() should throw for offer. " + tHolder);
        } catch (IllegalStateException ise) {
            // Ok
        }
    }

    private void verifyState(TaskHolder.State state) {
        assertEquals(state, MockUtils.getFieldValue(tHolder, "state"));
    }

    private void waitAssignedTo() {
        WatchDog wd = new WatchDog(10000L);
        try {
            while (assignedTo != pHolder.getProcessor()) {
                assertNull(SilentSleep.sleep(50L));
            }
        } finally {
            wd.done();
        }
        assertFalse(cancelled);
        assertFalse(timeout);
    }

    private void waitCancelled() {
        WatchDog wd = new WatchDog(10000L);
        try {
            while (!cancelled) {
                assertNull(SilentSleep.sleep(50L));
            }
        } finally {
            wd.done();
        }
        assertNull(assignedTo);
        assertFalse(timeout);
    }

    private void waitTimeout() {
        WatchDog wd = new WatchDog(10000L);
        try {
            while (!timeout) {
                assertNull(SilentSleep.sleep(50L));
            }
        } finally {
            wd.done();
        }
        assertNull(assignedTo);
        assertFalse(cancelled);
    }
}
