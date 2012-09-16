package com.cvortex.cc.atd.impl;

import static org.mockito.Mockito.*;

import org.junit.Assert;
import org.junit.Test;

public class ProcessHolderTest extends Assert {

    private final TaskHolder tHolder = mock(TaskHolder.class);
    
    private final ProcessorHolder pHolder = new ProcessorHolder(null);
    
    @Test
    public void isAcceptableTest() {
        when(tHolder.isAcceptableFor(pHolder)).thenReturn(true).thenReturn(false);
        assertTrue(pHolder.isAcceptableFor(tHolder));
        assertFalse(pHolder.isAcceptableFor(tHolder));
    }
    
    @Test
    public void offerTest() {
        pHolder.offer(tHolder);
        verify(tHolder, times(1)).offer(pHolder);
    }
    
    @Test
    public void removeTest() {
        assertTrue(pHolder.canBePlacedToQueue());
        pHolder.remove();
        assertFalse(pHolder.canBePlacedToQueue());
    }
    
}
