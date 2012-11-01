package org.cvortex.statistic;

import static org.mockito.Mockito.*;
import static junit.framework.Assert.*;

import java.util.Iterator;

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
    public void collection() {
        when(timeSource.currentTimeMillis()).thenReturn(1L);
        window.add(1L);
        when(timeSource.currentTimeMillis()).thenReturn(10L);
        window.add(1L);
        
        Iterator<IntervalledValue<Long>> it = window.getValues().iterator();
        assertEquals(0L, it.next().getTime());
        assertEquals(10L, it.next().getTime());
        assertFalse(it.hasNext());
    }
}
