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

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for formatting log messages.
 * <p>
 * This class brings the convenience of SLF4J-style message formatting to the Java Logger API. It allows for parameterized log messages
 * using "{}" as placeholders, which are replaced by the provided parameters.
 * <p>
 * This class is inspired by the design and structure of <a href="https://www.slf4j.org/">SLF4J</a>.
 */

public class LogMessageFormatter {

    public static final Pattern PLACEHOLDER = Pattern.compile("(?<!\\\\)\\{}");

    private LogMessageFormatter() {
        // Private constructor to prevent instantiation
    }

    /**
     * Formats a log message by replacing placeholders with the provided parameters. If the last parameter is a Throwable, it is treated
     * as the throwable for the formatted message.
     * <p>
     * Since this method is for logging, it is designed to be lenient and will not throw exceptions for null messages or parameters.
     * Instead, it will return a null message.
     *
     * @param message the message template with placeholders
     * @param params  the parameters to replace the placeholders
     * @return a FormattedMessage containing the formatted message and an optional throwable
     */
    static FormattedMessage formatMessage(String message, Object... params) {
        if (message == null) {
            // If the message is null, return a FormattedMessage with null message and no throwable
            return new FormattedMessage(null, null);
        }

        if (params == null || params.length == 0) {
            // No parameters provided, return the message as is
            // Convenience check
            return new FormattedMessage(message, null);
        }

        Throwable throwable = removeThrowableFromParams(params);
        if (null != throwable) {
            Object[] newParams = new Object[params.length - 1];
            // Copy all parameters except the last one (which is the throwable) to the new array
            System.arraycopy(params, 0, newParams, 0, params.length - 1);
            return formatMessage(message, throwable, newParams);
        }

        return formatMessage(message, null, params);
    }

    private static FormattedMessage formatMessage(String message, Throwable throwable, Object... params) {
        Matcher matcher = PLACEHOLDER.matcher(message);
        StringBuilder formattedMessage = new StringBuilder();

        int paramIndex = 0;

        while (matcher.find()) {
            if (paramIndex < params.length) {
                Object param = params[paramIndex++];
                String replacement = toString(param);
                matcher.appendReplacement(formattedMessage, Matcher.quoteReplacement(replacement));
            } else {
                break; // No more parameters to replace
            }
        }

        matcher.appendTail(formattedMessage);

        // Final step: Convert the StringBuilder to a String and
        // replace any escaped placeholders (i.e., "\{}") with "{}"
        String formattedMessageFinal = formattedMessage.toString().replaceAll("\\\\\\{}", "\\{}");

        return new FormattedMessage(formattedMessageFinal, throwable);

    }

    private static Throwable removeThrowableFromParams(Object[] params) {
        Object lastParam = params[params.length - 1];
        if (lastParam instanceof Throwable thrown) {
            return thrown;
        }

        return null;
    }

    /**
     * Null-safe toString(). This also makes sure object arrays are correctly converted to a string.
     * <p>
     * This method does not handle primitive arrays, as they are not expected to be used as parameters in log messages. If you need to
     * handle primitive arrays, you can add additional checks and conversions for each primitive type.
     *
     * @param obj object to convert
     * @return string representation of the object using toString() or "null" if the object is null
     */
    private static String toString(Object obj) {
        if (obj == null) {
            return "null";
        }

        if (obj instanceof Object[]) {
            return Arrays.toString((Object[]) obj);
        }

        return obj.toString();
    }


    record FormattedMessage(String message, Throwable throwable) {

    }
}
