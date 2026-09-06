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
package org.openntf.drapi.api;

import java.util.concurrent.CompletableFuture;
import org.openntf.drapi.http.DrapiRequest;
import org.openntf.drapi.meta.Document;
import org.openntf.drapi.meta.RichTextAs;

public interface DocumentsApi {

    CompletableFuture<Document> get(String documentId);

    CompletableFuture<Document> get(String documentId, GetOptions options);

    // TODO To make it more user-friendly, we could turn it into a Record and builder-style pattern.
    final class GetOptions {

        private String mode = null;
        private Boolean meta = null;
        private RichTextAs richTextAs = null;
        private Boolean markRead = null;
        private Boolean markUnread = null;

        public GetOptions withMode(String mode) {
            this.mode = mode;
            return this;
        }

        public GetOptions withMeta(Boolean meta) {
            this.meta = meta;
            return this;
        }

        public GetOptions withRichTextAs(RichTextAs richTextAs) {
            this.richTextAs = richTextAs;
            return this;
        }

        public GetOptions withMarkRead(Boolean markRead) {
            this.markRead = markRead;
            return this;
        }

        public GetOptions withMarkUnread(Boolean markUnread) {
            this.markUnread = markUnread;
            return this;
        }

        public String mode() {
            return mode;
        }

        public Boolean meta() {
            return meta;
        }

        public RichTextAs richTextAs() {
            return richTextAs;
        }

        public Boolean markRead() {
            return markRead;
        }

        public Boolean markUnread() {
            return markUnread;
        }

        public void applyToRequest(DrapiRequest request) {
            if (mode != null) {
                request.queryParam("mode", mode);
            }
            if (meta != null) {
                request.queryParam("meta", meta.toString());
            }
            if (richTextAs != null) {
                request.queryParam("richTextAs", richTextAs.value());
            }
            if (markRead != null) {
                request.queryParam("markRead", markRead.toString());
            }
            if (markUnread != null) {
                request.queryParam("markUnread", markUnread.toString());
            }
        }

    }
}
