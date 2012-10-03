package org.cvortex.transaction;

public class FailedTransactionException extends Exception {

    private static final long serialVersionUID = -8972702684434318889L;

    public FailedTransactionException(String message, Throwable causedBy) {
        super(message, causedBy);
    }
}
