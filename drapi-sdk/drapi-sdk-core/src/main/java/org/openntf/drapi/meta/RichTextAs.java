package org.openntf.drapi.meta;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

public final class RichTextAs {

    public static final RichTextAs HTML  = new RichTextAs("html");
    public static final RichTextAs PLAIN = new RichTextAs("plain");
    public static final RichTextAs MIME  = new RichTextAs("mime");
    public static final RichTextAs MARKDOWN  = new RichTextAs("markdown");

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

    public static RichTextAs of(String value) {
        return KNOWN.getOrDefault(value, new RichTextAs(value));
    }

    public static Collection<RichTextAs> known() {
        return KNOWN.values();
    }

}
