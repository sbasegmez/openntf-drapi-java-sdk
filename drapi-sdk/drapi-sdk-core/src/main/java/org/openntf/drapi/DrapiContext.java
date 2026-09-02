package org.openntf.drapi;

import org.openntf.drapi.http.HttpTransport;
import org.openntf.drapi.internal.auth.AuthenticationProvider;

public interface DrapiContext {

    DrapiConfig config();

    HttpTransport httpTransport();

    AuthenticationProvider authenticationProvider();

}
