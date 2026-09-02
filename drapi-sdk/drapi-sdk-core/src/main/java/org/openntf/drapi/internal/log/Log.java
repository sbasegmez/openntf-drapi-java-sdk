/*
 * Copyright (c) 2026 Serdar Basegmez
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openntf.drapi.internal.log;

import java.lang.System.Logger.Level;
import java.util.function.Supplier;
import org.openntf.drapi.internal.log.LogMessageFormatter.FormattedMessage;

/**
 * Thin wrapper around the logging framework used by the DRAPI SDK. This class is intended to be used internally by the SDK and should
 * not be used directly by external code.
 * <p>
 * SLF4j became a defacto standard for logging in Java. But for the sake of keeping dependency-free philosophy, we are mirroring SLF4j
 * idioms with this class and add some of the ease-of-use features to the Java Logger API.
 * <p>
 * This API is inspired by the design and structure of <a href="https://www.slf4j.org/">SLF4J</a>.
 */
public class Log {

    private final System.Logger delegate;

    private Log(String className) {
        this.delegate = System.getLogger(className);
    }

    /**
     * Get a logger for the given class. This method is intended to be used internally by the SDK and should not be used directly by
     * external code.
     *
     * @param clazz the class for which to get a logger
     * @return a logger for the given class
     */
    public static Log getLogger(Class<?> clazz) {
        return new Log(clazz.getName());
    }

    /*--------------------------------------------------------
       Delegate methods to the underlying System.Logger
     --------------------------------------------------------*/

    private boolean isLoggable(Level level) {
        return delegate.isLoggable(level);
    }

    private void log(Level level, String msg) {
        delegate.log(level, msg);
    }

    private void log(Level level, String msg, Throwable thrown) {
        delegate.log(level, msg, thrown);
    }

    private void log(Level level, Supplier<String> msgSupplier) {
        delegate.log(level, msgSupplier);
    }

    private void log(Level level, Supplier<String> msgSupplier, Throwable thrown) {
        delegate.log(level, msgSupplier, thrown);
    }

    // This method is used to log messages with parameters. We use our own implementation to format the message before passing it
    // to the underlying logger. Because we like SLF4j style formatting with {} placeholders.
    private void log(Level level, String format, Object... params) {
        // We want to avoid unnecessary string concatenation if the log level is not enabled
        if (isLoggable(level)) {
            FormattedMessage formattedMessage = LogMessageFormatter.formatMessage(format, params);
            delegate.log(level, formattedMessage.message(), formattedMessage.throwable());
        }
    }

    /*--------------------------------------------------------
       Convenience methods for logging at different levels
     --------------------------------------------------------*/

    public boolean isTraceEnabled() {
        return isLoggable(Level.TRACE);
    }

    public void trace(String msg) {
        log(Level.TRACE, msg);
    }

    public void trace(String msg, Throwable thrown) {
        log(Level.TRACE, msg, thrown);
    }

    public void trace(String format, Object... params) {
        log(Level.TRACE, format, params);
    }

    public void trace(Supplier<String> msgSupplier) {
        log(Level.TRACE, msgSupplier);
    }

    public void trace(Supplier<String> msgSupplier, Throwable thrown) {
        log(Level.TRACE, msgSupplier, thrown);
    }

    /*--------------------------------------------------------*/

    public boolean isDebugEnabled() {
        return isLoggable(Level.DEBUG);
    }

    public void debug(String msg) {
        log(Level.DEBUG, msg);
    }

    public void debug(String msg, Throwable thrown) {
        log(Level.DEBUG, msg, thrown);
    }

    public void debug(String format, Object... params) {
        log(Level.DEBUG, format, params);
    }

    public void debug(Supplier<String> msgSupplier) {
        log(Level.DEBUG, msgSupplier);
    }

    public void debug(Supplier<String> msgSupplier, Throwable thrown) {
        log(Level.DEBUG, msgSupplier, thrown);
    }

    /*--------------------------------------------------------*/

    public boolean isInfoEnabled() {
        return isLoggable(Level.INFO);
    }

    public void info(String msg) {
        log(Level.INFO, msg);
    }

    public void info(String msg, Throwable thrown) {
        log(Level.INFO, msg, thrown);
    }

    public void info(String format, Object... params) {
        log(Level.INFO, format, params);
    }

    public void info(Supplier<String> msgSupplier) {
        log(Level.INFO, msgSupplier);
    }

    public void info(Supplier<String> msgSupplier, Throwable thrown) {
        log(Level.INFO, msgSupplier, thrown);
    }

    /*--------------------------------------------------------*/

    public boolean isWarnEnabled() {
        return isLoggable(Level.WARNING);
    }

    public void warn(String msg) {
        log(Level.WARNING, msg);
    }

    public void warn(String msg, Throwable thrown) {
        log(Level.WARNING, msg, thrown);
    }

    public void warn(String format, Object... params) {
        log(Level.WARNING, format, params);
    }

    public void warn(Supplier<String> msgSupplier) {
        log(Level.WARNING, msgSupplier);
    }

    public void warn(Supplier<String> msgSupplier, Throwable thrown) {
        log(Level.WARNING, msgSupplier, thrown);
    }

    /*--------------------------------------------------------*/

    public boolean isErrorEnabled() {
        return isLoggable(Level.ERROR);
    }

    public void error(String msg) {
        log(Level.ERROR, msg);
    }

    public void error(String msg, Throwable thrown) {
        log(Level.ERROR, msg, thrown);
    }

    public void error(String format, Object... params) {
        log(Level.ERROR, format, params);
    }

    public void error(Supplier<String> msgSupplier) {
        log(Level.ERROR, msgSupplier);
    }

    public void error(Supplier<String> msgSupplier, Throwable thrown) {
        log(Level.ERROR, msgSupplier, thrown);
    }

}
