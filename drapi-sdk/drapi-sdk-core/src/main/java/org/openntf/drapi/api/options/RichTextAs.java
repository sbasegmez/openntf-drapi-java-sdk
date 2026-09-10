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
package org.openntf.drapi.api.options;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

/**
 * Class representing the format of rich text content. Used by various APIs.
 * <p>
 * Since DRAPI has extensibility in terms of rich text formats, this class allows for both predefined constants and custom formats.
 *
 */
public final class RichTextAs {

    // Predefined constants for known rich text formats
    public static final RichTextAs HTML = new RichTextAs("html");
    public static final RichTextAs PLAIN = new RichTextAs("plain");
    public static final RichTextAs MIME = new RichTextAs("mime");
    public static final RichTextAs MARKDOWN = new RichTextAs("markdown");

    private static final Map<String, RichTextAs> KNOWN = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    static {
        KNOWN.put(HTML.value(), HTML);
        KNOWN.put(PLAIN.value(), PLAIN);
        KNOWN.put(MIME.value(), MIME);
        KNOWN.put(MARKDOWN.value(), MARKDOWN);
    }

    private final String value;

    private RichTextAs(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    /**
     * Returns a RichTextAs instance for the given value. If the value is known, it returns the corresponding constant; otherwise, it
     * creates a new instance.
     *
     * @param value the string representation of the rich text format
     * @return a RichTextAs instance corresponding to the given value
     */
    public static RichTextAs of(String value) {
        return KNOWN.getOrDefault(value, new RichTextAs(value));
    }

    public static Collection<RichTextAs> known() {
        return Collections.unmodifiableCollection(KNOWN.values());
    }

}
