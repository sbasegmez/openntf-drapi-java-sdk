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
module org.openntf.drapi {

    requires java.net.http;

    exports org.openntf.drapi;
    exports org.openntf.drapi.api;
    exports org.openntf.drapi.exception;
    exports org.openntf.drapi.http;
    exports org.openntf.drapi.json;
    exports org.openntf.drapi.meta;
    exports org.openntf.drapi.util;

    // Some classes (e.g. AuthRequest, AuthResponse) are (de)serialized by whichever JsonBindingProvider is on the module path,
    // so they need to stay reflectively accessible even though all other internal packages themselves are not exported.
    // ErrorMessage needs no such opens: it's a fully public record in an already-exported package, and reflection on public members
    // of an exported type doesn't require it.
    opens org.openntf.drapi.internal.dto;

    uses org.openntf.drapi.json.JsonBindingProvider;
    uses org.openntf.drapi.http.HttpTransportProvider;

}
