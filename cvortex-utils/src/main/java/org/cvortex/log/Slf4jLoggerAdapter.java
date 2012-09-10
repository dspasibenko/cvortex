package org.cvortex.log;

import org.slf4j.Logger;

final class Slf4jLoggerAdapter extends AbstractLogger {

    private final Logger slf4jLogger;

    Slf4jLoggerAdapter(Logger slf4jLogger, String formatString, Object marker) {
        super(formatString, marker);
        this.slf4jLogger = slf4jLogger;
    }
    
    @Override
    public boolean isEnabled(LogLevel logLevel) {
        switch(logLevel) {
        case FATAL: return true;
        case ERROR: return slf4jLogger.isErrorEnabled();
        case WARN: return slf4jLogger.isWarnEnabled();
        case INFO: return slf4jLogger.isInfoEnabled();
        case DEBUG: return slf4jLogger.isDebugEnabled();
        case TRACE: return slf4jLogger.isTraceEnabled();
        }
        return true;
    }

    @Override
    public void log(LogLevel logLevel, String message) {
        switch(logLevel) {
        case FATAL: slf4jLogger.error("=== FATAL ERROR ===" + message); break;
        case ERROR: slf4jLogger.error(message); break;
        case WARN: slf4jLogger.warn(message); break;
        case INFO: slf4jLogger.info(message); break;
        case DEBUG: slf4jLogger.debug(message); break;
        case TRACE: slf4jLogger.trace(message); break;
        }
    }
}
