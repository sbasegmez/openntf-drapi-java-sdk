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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the {@link Log} class.
 * <p>
 * We will use a custom log handler to capture log messages and verify that the logging methods work as expected.
 * <p>
 * Log Levels:
 *
 * <ol>
 *     <li>FINER => TRACE</li>
 *     <li>FINE => DEBUG</li>
 *     <li>INFO => INFO</li>
 *     <li>WARNING => WARN</li>
 *     <li>SEVERE => ERROR</li>
 * </ol>
 *
 */
class LogTest {

    private Logger realLogger;
    private TestLogHandler testLogHandler;

    @BeforeEach
    void setUp() {
        this.testLogHandler = new TestLogHandler();
        this.realLogger = Logger.getLogger(LogTest.class.getName());

        this.realLogger.setUseParentHandlers(false); // Disable parent handlers to avoid duplicate logs
        this.realLogger.addHandler(this.testLogHandler);
    }

    @AfterEach
    void tearDown() {
        if (this.realLogger != null) {
            this.realLogger.removeHandler(this.testLogHandler);
        }
    }

    @Test
    @DisplayName("Test that Log.getLogger() returns a non-null logger instance")
    void testGetLogger() {
        Log logger = Log.getLogger(LogTest.class);
        assertNotNull(logger);
    }

    @Test
    @DisplayName("Test trace(..) methods")
    void testTraceMethods() {
        realLogger.setLevel(Level.FINER); // Enable TRACE level logging

        Log log = Log.getLogger(LogTest.class);

        assertTrue(log.isTraceEnabled(), "Trace level should be enabled");

        log.trace("This is a message");
        log.trace("Another message", new RuntimeException("Test exception"));
        log.trace("One more message with params: {} and {}", "param1", 42);
        log.trace(() -> "This is a message from supplier");
        log.trace(() -> "Another message from supplier", new RuntimeException("Test exception"));
        log.trace("One last message with params and throwable: {} and {}", "param1", 42, new RuntimeException("Test exception"));

        // Verify that the log records were captured
        assertEquals(6, testLogHandler.records().size(), "Expected six log records to be captured");

        assertLogRecord(0, "Trace", Level.FINER, "This is a message");
        assertLogRecord(1, "Trace", Level.FINER, "Another message", RuntimeException.class, "Test exception");
        assertLogRecord(2, "Trace", Level.FINER, "One more message with params: param1 and 42");
        assertLogRecord(3, "Trace", Level.FINER, "This is a message from supplier");
        assertLogRecord(4, "Trace", Level.FINER, "Another message from supplier", RuntimeException.class, "Test exception");
        assertLogRecord(5, "Trace", Level.FINER, "One last message with params and throwable: param1 and 42", RuntimeException.class, "Test exception");
    }

    @Test
    @DisplayName("Test debug(..) methods")
    void testDebugMethods() {
        realLogger.setLevel(Level.FINE); // Enable DEBUG level logging

        Log log = Log.getLogger(LogTest.class);

        assertTrue(log.isDebugEnabled(), "Debug level should be enabled");

        log.debug("This is a message");
        log.debug("Another message", new RuntimeException("Test exception"));
        log.debug("One more message with params: {} and {}", "param1", 42);
        log.debug(() -> "This is a message from supplier");
        log.debug(() -> "Another message from supplier", new RuntimeException("Test exception"));
        log.debug("One last message with params and throwable: {} and {}", "param1", 42, new RuntimeException("Test exception"));

        // Verify that the log records were captured
        assertEquals(6, testLogHandler.records().size(), "Expected six log records to be captured");

        assertLogRecord(0, "Debug", Level.FINE, "This is a message");
        assertLogRecord(1, "Debug", Level.FINE, "Another message", RuntimeException.class, "Test exception");
        assertLogRecord(2, "Debug", Level.FINE, "One more message with params: param1 and 42");
        assertLogRecord(3, "Debug", Level.FINE, "This is a message from supplier");
        assertLogRecord(4, "Debug", Level.FINE, "Another message from supplier", RuntimeException.class, "Test exception");
        assertLogRecord(5, "Debug", Level.FINE, "One last message with params and throwable: param1 and 42", RuntimeException.class, "Test exception");
    }

    @Test
    @DisplayName("Test info(..) methods")
    void testInfoMethods() {
        realLogger.setLevel(Level.INFO); // Enable INFO level logging

        Log log = Log.getLogger(LogTest.class);

        assertTrue(log.isInfoEnabled(), "Info level should be enabled");

        log.info("This is a message");
        log.info("Another message", new RuntimeException("Test exception"));
        log.info("One more message with params: {} and {}", "param1", 42);
        log.info(() -> "This is a message from supplier");
        log.info(() -> "Another message from supplier", new RuntimeException("Test exception"));
        log.info("One last message with params and throwable: {} and {}", "param1", 42, new RuntimeException("Test exception"));

        // Verify that the log records were captured
        assertEquals(6, testLogHandler.records().size(), "Expected six log records to be captured");

        assertLogRecord(0, "Info", Level.INFO, "This is a message");
        assertLogRecord(1, "Info", Level.INFO, "Another message", RuntimeException.class, "Test exception");
        assertLogRecord(2, "Info", Level.INFO, "One more message with params: param1 and 42");
        assertLogRecord(3, "Info", Level.INFO, "This is a message from supplier");
        assertLogRecord(4, "Info", Level.INFO, "Another message from supplier", RuntimeException.class, "Test exception");
        assertLogRecord(5, "Info", Level.INFO, "One last message with params and throwable: param1 and 42", RuntimeException.class, "Test exception");
    }

    @Test
    @DisplayName("Test warn(..) methods")
    void testWarnMethods() {
        realLogger.setLevel(Level.WARNING); // Enable WARN level logging

        Log log = Log.getLogger(LogTest.class);

        assertTrue(log.isWarnEnabled(), "Warn level should be enabled");

        log.warn("This is a message");
        log.warn("Another message", new RuntimeException("Test exception"));
        log.warn("One more message with params: {} and {}", "param1", 42);
        log.warn(() -> "This is a message from supplier");
        log.warn(() -> "Another message from supplier", new RuntimeException("Test exception"));
        log.warn("One last message with params and throwable: {} and {}", "param1", 42, new RuntimeException("Test exception"));

        // Verify that the log records were captured
        assertEquals(6, testLogHandler.records().size(), "Expected six log records to be captured");

        assertLogRecord(0, "Warn", Level.WARNING, "This is a message");
        assertLogRecord(1, "Warn", Level.WARNING, "Another message", RuntimeException.class, "Test exception");
        assertLogRecord(2, "Warn", Level.WARNING, "One more message with params: param1 and 42");
        assertLogRecord(3, "Warn", Level.WARNING, "This is a message from supplier");
        assertLogRecord(4, "Warn", Level.WARNING, "Another message from supplier", RuntimeException.class, "Test exception");
        assertLogRecord(5, "Warn", Level.WARNING, "One last message with params and throwable: param1 and 42", RuntimeException.class, "Test exception");
    }

    @Test
    @DisplayName("Test error(..) methods")
    void testErrorMethods() {
        realLogger.setLevel(Level.SEVERE); // Enable ERROR level logging

        Log log = Log.getLogger(LogTest.class);

        assertTrue(log.isErrorEnabled(), "Error level should be enabled");

        log.error("This is a message");
        log.error("Another message", new RuntimeException("Test exception"));
        log.error("One more message with params: {} and {}", "param1", 42);
        log.error(() -> "This is a message from supplier");
        log.error(() -> "Another message from supplier", new RuntimeException("Test exception"));
        log.error("One last message with params and throwable: {} and {}", "param1", 42, new RuntimeException("Test exception"));

        // Verify that the log records were captured
        assertEquals(6, testLogHandler.records().size(), "Expected six log records to be captured");

        assertLogRecord(0, "Error", Level.SEVERE, "This is a message");
        assertLogRecord(1, "Error", Level.SEVERE, "Another message", RuntimeException.class, "Test exception");
        assertLogRecord(2, "Error", Level.SEVERE, "One more message with params: param1 and 42");
        assertLogRecord(3, "Error", Level.SEVERE, "This is a message from supplier");
        assertLogRecord(4, "Error", Level.SEVERE, "Another message from supplier", RuntimeException.class, "Test exception");
        assertLogRecord(5, "Error", Level.SEVERE, "One last message with params and throwable: param1 and 42", RuntimeException.class, "Test exception");
    }

    @Test
    @DisplayName("Test that logging ignores messages below the current log level")
    void testLoggingIgnoresMessagesBelowCurrentLogLevel() {
        realLogger.setLevel(Level.WARNING); // Set log level to WARN

        Log log = Log.getLogger(LogTest.class);

        assertTrue(log.isWarnEnabled(), "Warn level should be enabled");
        assertTrue(log.isErrorEnabled(), "Error level should be enabled");
        assertFalse(log.isInfoEnabled(), "Info level should NOT be enabled");
        assertFalse(log.isDebugEnabled(), "Debug level should NOT be enabled");
        assertFalse(log.isTraceEnabled(), "Trace level should NOT be enabled");

        log.trace("This is a trace message");
        log.debug("This is a debug message");
        log.info("This is an info message");
        log.warn("This is a warn message");
        log.error("This is an error message");

        // Verify that only the WARN and ERROR messages were captured
        assertEquals(2, testLogHandler.records().size(), "Expected two log records to be captured");

        assertLogRecord(0, "Warn", Level.WARNING, "This is a warn message");
        assertLogRecord(1, "Error", Level.SEVERE, "This is an error message");
    }

    private void assertLogRecord(int recordIndex, String assertionPrefix, Level expectedLevel, String expectedMessage) {
        assertLogRecord(recordIndex, assertionPrefix, expectedLevel, expectedMessage, null, null);
    }

    private void assertLogRecord(int recordIndex, String assertionPrefix, Level expectedLevel, String expectedMessage, Class<? extends Throwable> expectedThrowableClass, String expectedThrowableMessage) {
        LogRecord record = testLogHandler.records.get(recordIndex);

        assertEquals(expectedLevel, record.getLevel(), assertionPrefix + " - Log record level should match");
        assertEquals(expectedMessage, record.getMessage(), assertionPrefix + " - Log record message should match");
        if (expectedThrowableClass != null) {
            assertNotNull(record.getThrown(), assertionPrefix + " - Log record should have a throwable");
            assertInstanceOf(expectedThrowableClass, record.getThrown(), assertionPrefix + " - Log record throwable class should match");
            assertEquals(expectedThrowableMessage, record.getThrown().getMessage(), assertionPrefix + " - Log record throwable message should match");
        } else {
            assertNull(record.getThrown(), assertionPrefix + " - Log record should not have a throwable");
        }
    }

    static class TestLogHandler extends Handler {

        private final List<LogRecord> records = new ArrayList<>();

        public List<LogRecord> records() {
            return records;
        }

        public LogRecord lastRecord() {
            if (records.isEmpty()) {
                return null;
            }
            return records.get(records.size() - 1);
        }

        @Override
        public void publish(LogRecord record) {
            records.add(record);
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() throws SecurityException {
        }
    }

}
