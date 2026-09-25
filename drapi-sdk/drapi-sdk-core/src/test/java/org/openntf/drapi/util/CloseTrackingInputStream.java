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
