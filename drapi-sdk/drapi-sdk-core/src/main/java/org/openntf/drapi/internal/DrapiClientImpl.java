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

import java.util.Objects;
import org.openntf.drapi.Drapi;
import org.openntf.drapi.DrapiClient;
import org.openntf.drapi.DrapiDataSource;
import org.openntf.drapi.auth.SessionContext;
import org.openntf.drapi.util.TypeUtils;

public class DrapiClientImpl implements DrapiClient {

    private final Drapi parent;
    private final SessionContext sessionContext;

    public DrapiClientImpl(Drapi parent, SessionContext sessionContext) {
        this.parent = Objects.requireNonNull(parent, "Drapi must not be null");
        this.sessionContext = Objects.requireNonNull(sessionContext, "SessionContext must not be null");
    }

    @Override
    public Drapi parent() {
        return parent;
    }

    @Override
    public SessionContext sessionContext() {
        return sessionContext;
    }

    @Override
    public DrapiDataSource dataSource(String name) {
        TypeUtils.requireNonEmpty(name, "Data source name must not be null or empty");

        return new DrapiDataSourceImpl(this, name);
    }

    @Override
    public void logout() {
        parent.logout(sessionContext);
    }
}
