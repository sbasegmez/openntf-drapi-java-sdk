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
package org.openntf.drapi;

import org.openntf.drapi.auth.SessionContext;
import org.openntf.drapi.auth.TokenSourceProvider;
import org.openntf.drapi.http.HttpTransport;

/**
 * The Drapi interface defines the root element for the SDK. It serves as the entry point for accessing the various components and
 * functionalities provided by the SDK. Implementations of this interface will provide concrete behavior for interacting with the
 * underlying system or framework.
 */
public interface Drapi {

    /**
     * Returns the configuration settings for the Drapi instance. The configuration may include various parameters and options that
     * influence the behavior of the SDK.
     *
     * @return the DrapiConfig instance containing the configuration settings
     */
    DrapiConfig config();

    /**
     * Returns the HTTP transport mechanism used for making network requests. This transport is responsible for handling the
     * communication between the SDK and external services or APIs.
     * <p>
     * FIXME: This method should be removed from the interface. It's not safe to keep a public reference to the authenticating transport.
     *
     * @return the HttpTransport instance used for network communication
     */
    HttpTransport transport();

    /**
     * Creates a new DrapiClient instance for the specified session context. The DrapiClient provides methods for interacting with the
     * underlying system or framework within the context of the provided session.
     *
     * @param sessionContext the SessionContext containing session-specific information
     * @return a new DrapiClient instance for the specified session context
     */
    DrapiClient forSession(SessionContext sessionContext);

    /**
     * Logs out the user associated with the specified session context. This method will invalidate any authentication tokens and
     * perform necessary cleanup to ensure that the user is logged out of the system.
     * <p>
     * This method is here to shield TokenSource from other classes.
     *
     * @param sessionContext the SessionContext containing session-specific information for the user to be logged out
     */
    void logout(SessionContext sessionContext);

    /**
     * Shuts down the Drapi instance, releasing any resources and performing necessary cleanup. This method should be called when the
     * Drapi instance is no longer needed to ensure proper resource management.
     */
    void shutdown();

    /**
     * Creates a new DrapiBuilder instance for constructing a Drapi instance with the specified configuration.
     *
     * @param config              the DrapiConfig containing configuration settings
     * @param tokenSourceProvider the TokenSourceProvider used for authentication
     * @return a new DrapiBuilder instance
     */
    static DrapiBuilder builder(DrapiConfig config, TokenSourceProvider tokenSourceProvider) {
        return new DrapiBuilder(config, tokenSourceProvider);
    }

}
