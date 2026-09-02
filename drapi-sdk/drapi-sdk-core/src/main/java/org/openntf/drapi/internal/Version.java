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
package org.openntf.drapi.internal;

import java.io.IOException;
import java.util.Properties;

public final class Version {

    private static final String VERSION;

    static {
        try (var input = Version.class
            .getResourceAsStream("/version.properties")) {

            var properties = new Properties();
            properties.load(input);

            VERSION = properties.getProperty("version");
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public static String get() {
        return VERSION;
    }

    private Version() {
    }
}
