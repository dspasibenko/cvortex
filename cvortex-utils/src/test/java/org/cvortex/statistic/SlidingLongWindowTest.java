package org.cvortex.statistic;

import static org.mockito.Mockito.*;
import static junit.framework.Assert.*;

import org.cvortex.env.TimeSource;
import org.cvortex.util.testing.MockUtils;
import org.junit.Before;
import org.junit.Test;

public class SlidingLongWindowTest {

    private TimeSource timeSource = mock(TimeSource.class);
    
    private SlidingLongWindow window = new SlidingLongWindow(15, 5);
    
    @Before
    public void init() {
        MockUtils.setFieldValue(TimeBasedSlidingWindow.class, window, "timeSource", timeSource);
    }

    @Test
    public void empty() {
        assertEquals(new Long(0L), new Long(window.getSum()));
    }
    
    @Test
    public void sameBucket() {
        when(timeSource.currentTimeMillis()).thenReturn(1L);
        window.add(1L);
        when(timeSource.currentTimeMillis()).thenReturn(2L);
        window.add(1L);
        when(timeSource.currentTimeMillis()).thenReturn(3L);
        window.add(1L);
        assertEquals(new Long(3L), new Long(window.getSum()));
        when(timeSource.currentTimeMillis()).thenReturn(16L);
        assertEquals(new Long(0L), new Long(window.getSum()));
    }
    
    @Test
    public void diffBuckets() {
        when(timeSource.currentTimeMillis()).thenReturn(1L);
        window.add(1L);
        when(timeSource.currentTimeMillis()).thenReturn(6L);
        window.add(1L);
        when(timeSource.currentTimeMillis()).thenReturn(10L);
        window.add(1L);
        assertEquals(new Long(3L), new Long(window.getSum()));
        when(timeSource.currentTimeMillis()).thenReturn(16L);
        assertEquals(new Long(2L), new Long(window.getSum()));
        when(timeSource.currentTimeMillis()).thenReturn(21L);
        assertEquals(new Long(1L), new Long(window.getSum()));
        when(timeSource.currentTimeMillis()).thenReturn(26L);
        assertEquals(new Long(0L), new Long(window.getSum()));
    }
    
    @Test
    public void diffBuckets2() {
        when(timeSource.currentTimeMillis()).thenReturn(1L);
        window.add(1L);
        when(timeSource.currentTimeMillis()).thenReturn(10L);
        window.add(1L);
        assertEquals(new Long(2L), new Long(window.getSum()));
        when(timeSource.currentTimeMillis()).thenReturn(16L);
        assertEquals(new Long(1L), new Long(window.getSum()));
        when(timeSource.currentTimeMillis()).thenReturn(21L);
        assertEquals(new Long(1L), new Long(window.getSum()));
        when(timeSource.currentTimeMillis()).thenReturn(26L);
        assertEquals(new Long(0L), new Long(window.getSum()));
    }
    
    @Test
    public void decreaseSize() {
        when(timeSource.currentTimeMillis()).thenReturn(1L);
        window.add(1L);
        when(timeSource.currentTimeMillis()).thenReturn(6L);
        window.add(2L);
        when(timeSource.currentTimeMillis()).thenReturn(10L);
        window.add(3L);
        assertEquals(new Long(6L), new Long(window.getSum()));
        window.setSize(10);
        when(timeSource.currentTimeMillis()).thenReturn(12L);
        assertEquals(new Long(5L), new Long(window.getSum()));
        window.setSize(5);
        assertEquals(new Long(3L), new Long(window.getSum()));
    }
    
    @Test
    public void increaseSize() {
        when(timeSource.currentTimeMillis()).thenReturn(1L);
        window.add(1L);
        assertEquals(new Long(1L), new Long(window.getSum()));
        window.setSize(20);
        when(timeSource.currentTimeMillis()).thenReturn(18L);
        assertEquals(new Long(1L), new Long(window.getSum()));
    }
}
