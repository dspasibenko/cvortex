package org.cvortex.transaction;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;

import org.jrivets.log.Logger;
import org.jrivets.log.LoggerFactory;

/**
 * Contract: <code>Action.doAction()</code> are executed in submit order. If the method
 * throws an exception the whole transaction rolled back immediately (all
 * previously success actions are rolled back by calling <code>Action.rallbackAction()</code>
 * in reverse order of their submissions. In case of the roll-back procedure 
 * throws an exception, the exception is silently ignored.)
 * 
 * @author Dmitry Spasibenko
 * 
 */
public final class SimpleTransaction {
    
    private final Logger logger;

    private final String name;
    
    private final long constructThreadId;

    private final List<Action> actions = new ArrayList<Action>();

    private volatile State state = State.INIT;

    private enum State {
        INIT, RUNNING, FAILED, COMMITED, CANCELLED;
        private boolean isFinal() {
            return this.equals(FAILED) || this.equals(COMMITED) || this.equals(CANCELLED);
        }
    }

    public SimpleTransaction(String transactionName) {
        this.logger = LoggerFactory.getLogger(SimpleTransaction.class, transactionName + " %2$s", null);
        this.name = transactionName;
        this.constructThreadId = Thread.currentThread().getId();
    }
    
    private void checkInvoker() {
        if (Thread.currentThread().getId() != constructThreadId) {
            throw new ConcurrentModificationException("Concurrency violation: the invoker is not the creator invoker: "
                    + Thread.currentThread() + this);
        }
    }

    private void checkToRun(String actionName) {
        checkInvoker();
        if (state.isFinal()) {
            logger.warn(actionName, " cannot be called in final state ", this);
            throw new IllegalStateException("Wrong state to " + actionName + this);
        }
        if (state.equals(State.INIT)) {
            state = State.RUNNING;
        }
    }

    public SimpleTransaction doAction(Action action) throws FailedTransactionException {
        checkToRun("doAction()");

        try {
            logger.debug("Executing action ", action);
            action.doAction();
            actions.add(action);
            logger.debug("Successfully done.");
        } catch (Throwable t) {
            logger.debug("Failed: ", t);
            state = State.FAILED;
            rollAllBack();
            throw new FailedTransactionException("Transaction failed when running action " + action, t);
        }
        return this;
    }

    private void rollAllBack() {
        logger.debug("Rolling back ", actions.size(), " actions");
        while (!actions.isEmpty()) {
            try {
                actions.remove(actions.size() - 1).rollbackAction();
            } catch (Throwable t) {
                logger.error("Ignore exception silently in rolling-back action. Action.rollback() should not throw exceptions, please investigate. ", t);
            }
        }
    }

    public void commit() {
        checkToRun("commit()");
        state = State.COMMITED;
        actions.clear();
    }

    public void cancel() {
        checkToRun("cancel()");
        state = State.CANCELLED;
        rollAllBack();
    }
    
    @Override
    public String toString() {
        return new StringBuilder().append("SimpleTransaction: {\"").append(name).append("\", Tid= ").append(constructThreadId)
                .append(", state=").append(state).append(" }").toString();
    }
}
