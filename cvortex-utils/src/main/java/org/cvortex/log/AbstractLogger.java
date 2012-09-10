package org.cvortex.log;

public abstract class AbstractLogger implements Logger {

    enum LogLevel {
        FATAL, 
        ERROR,
        WARN, 
        INFO,
        DEBUG,
        TRACE
    }
    
    private Object marker;
    
    private final String formatString;
    
    AbstractLogger(String formatString, Object marker) {
        this.formatString = formatString;
        this.marker = marker;
    }
    
    @Override
    public void fatal(Object... args) {
        logWithLevel(LogLevel.FATAL, args);
    }

    @Override
    public void error(Object... args) {
        logWithLevel(LogLevel.ERROR, args);
    }

    @Override
    public void warn(Object... args) {
        logWithLevel(LogLevel.WARN, args);
    }

    @Override
    public void info(Object... args) {
        logWithLevel(LogLevel.INFO, args);
    }

    @Override
    public void debug(Object... args) {
        logWithLevel(LogLevel.DEBUG, args);
    }

    @Override
    public void trace(Object... args) {
        logWithLevel(LogLevel.TRACE, args);
    }

    public void setMarker(Object marker) {
        this.marker = marker;
    }
    
    public abstract boolean isEnabled(LogLevel logLevel);
    public abstract void log(LogLevel logLevel, String message);

    private void logWithLevel(LogLevel logLevel, Object ... args) {
        if (isEnabled(logLevel)) {
            String message = Formatter.concatArgs(args);
            if (formatString != null) {
                log(logLevel, String.format(formatString, marker, message));
            } else {
                log(logLevel, message);
            }
        }
    }
}
