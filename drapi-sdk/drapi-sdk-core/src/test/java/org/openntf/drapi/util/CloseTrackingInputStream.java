package org.openntf.drapi.util;

import java.io.ByteArrayInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * A simple InputStream that tracks whether it has been closed.
 * This is useful for testing purposes to ensure that streams are properly closed.
 */
public class CloseTrackingInputStream extends FilterInputStream {

    private boolean closed = false;

    public CloseTrackingInputStream(String json) {
        super(new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)));
    }

    public boolean isClosed() {
        return closed;
    }

    @Override
    public void close() throws IOException {
        closed = true;
        super.close();
    }
}
