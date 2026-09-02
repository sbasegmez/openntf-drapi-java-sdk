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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class LogMessageFormatterTest {

    @Test
    @DisplayName("Test formatMessage with no parameters")
    void testFormatMessageWithNoParams() {
        var result = LogMessageFormatter.formatMessage("Test message");

        assertEquals("Test message", result.message(), "The message should be returned as is when no parameters are provided");
    }

    @Test
    @DisplayName("Test formatMessage with null message")
    void testFormatMessageWithNullMessage() {
        var result = LogMessageFormatter.formatMessage(null);

        assertNull(result.message(), "The message should be returned as is when no message are provided");
    }

    @Test
    @DisplayName("Test formatMessage with null message with parameters")
    void testFormatMessageWithNullMessageWithParameters() {
        var result = LogMessageFormatter.formatMessage(null, "param1", "param2");

        assertNull(result.message(), "The message should be returned as is when no message are provided");
    }

    @Test
    @DisplayName("Test formatMessage with null parameters")
    void testFormatMessageWithNullParameters() {
        var result = LogMessageFormatter.formatMessage("Test message", (Object[]) null);

        assertEquals("Test message", result.message(), "The message should be returned as is when no parameters are provided");
    }

    @Test
    @DisplayName("Test formatMessage with a simple replacement")
    void testFormatMessageWithSimpleReplacement() {
        var result = LogMessageFormatter.formatMessage("Hello, {} {}!", "World", "Test");

        assertEquals("Hello, World Test!", result.message(), "The placeholder should be replaced with the provided parameter");
        assertNull(result.throwable(), "There should be no throwable when none is provided");
    }

    @Test
    @DisplayName("Test formatMessage with an escaped placeholder")
    void testFormatMessageWithEscapedPlaceholder() {
        var result = LogMessageFormatter.formatMessage("Hello, \\{} {}!", "World", "Test");

        assertEquals("Hello, {} World!", result.message(), "The escaped placeholder should not be replaced with the provided parameter");
        assertNull(result.throwable(), "There should be no throwable when none is provided");
    }

    @Test
    @DisplayName("Test formatMessage with placeholder in start and end of the message")
    void testFormatMessageWithPlaceholderInStartAndEnd() {
        var result = LogMessageFormatter.formatMessage("{}, {}", "Hello", "World");

        assertEquals("Hello, World", result.message(), "The placeholder can be in the start or end of the message");
        assertNull(result.throwable(), "There should be no throwable when none is provided");
    }

    @Test
    @DisplayName("Test formatMessage with a simple replacement and throwable as the last parameter")
    void testFormatMessageWithSimpleReplacementAndThrowable() {
        var result = LogMessageFormatter.formatMessage("Hello, {} {}!", "World", "Test", new RuntimeException("Test Exception"));

        assertEquals("Hello, World Test!", result.message(), "The placeholder should be replaced with the provided parameter");
        assertNotNull(result.throwable(), "There should be a throwable when one is provided");
        assertEquals("Test Exception", result.throwable().getMessage(), "The throwable should be the one provided in the parameters");
    }

    @Test
    @DisplayName("Test formatMessage with a null within parameters")
    void testFormatMessageWithNullWithinParameters() {
        var result = LogMessageFormatter.formatMessage("Hello, {} {}!", "World", null);

        assertEquals("Hello, World null!", result.message(), "The placeholder should be replaced with \"null\" for null parameters");
        assertNull(result.throwable(), "There should be no throwable when none is provided");
    }

    @Test
    @DisplayName("Test formatMessage with a more placeholders than parameters")
    void testFormatMessageWithMorePlaceholdersThanParameters() {
        var result = LogMessageFormatter.formatMessage("Hello, {} {} {}!", "World", "Test");

        assertEquals("Hello, World Test {}!", result.message(), "Unmatched placeholders should remain in the message when there are more placeholders than parameters");
        assertNull(result.throwable(), "There should be no throwable when none is provided");
    }

    @Test
    @DisplayName("Test formatMessage with more parameters than placeholders")
    void testFormatMessageWithMoreParametersThanPlaceholders() {
        var result = LogMessageFormatter.formatMessage("Hello, {} {}!", "World", "Test", "Extra");

        assertEquals("Hello, World Test!", result.message(), "Unmatched parameters should be ignored when there are more parameters than placeholders");
        assertNull(result.throwable(), "There should be no throwable when none is provided");
    }

    @Test
    @DisplayName("Test formatMessage with placeholder and throwable as the only parameter")
    void testFormatMessageWithPlaceholderAndThrowableAsTheOnlyParameter() {
        var result = LogMessageFormatter.formatMessage("Hello, {}!", new RuntimeException("Test Exception"));

        assertEquals("Hello, {}!", result.message(), "Throwable should not be treated as a parameter when there are no other parameters to replace the placeholder");
        assertNotNull(result.throwable(), "There should be a throwable when one is provided");
        assertEquals("Test Exception", result.throwable().getMessage(), "The throwable should be the one provided in the parameters");
    }

    @Test
    @DisplayName("Test formatMessage with an exception within parameters but not the last")
    void testFormatMessageWithExceptionWithinParametersButNotTheLast() {
        var thrown = new RuntimeException("Test Exception");
        var result = LogMessageFormatter.formatMessage("Hello, {} {}!", "World", thrown, "Ignore this");

        assertEquals("Hello, World " + thrown
                         + "!", result.message(), "Exception should be treated as a normal parameter when it is not the last parameter");
        assertNull(result.throwable(), "There should be no throwable when one is not the last parameter");
    }

    @Test
    @DisplayName("Test formatMessage with an object array within parameters")
    void testFormatMessageWithObjectArrayWithinParameters() {
        var result = LogMessageFormatter.formatMessage("Hello, {} {}!", "World", new Object[]{"Test", "Array"});

        assertEquals("Hello, World [Test, Array]!", result.message(), "Object arrays should be correctly converted to a string");
    }
}
