package org.cvortex.transaction;

public interface Action {

    public void doAction();

    public void rollbackAction();
    
}
