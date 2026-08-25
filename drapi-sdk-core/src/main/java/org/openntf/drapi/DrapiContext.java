package org.openntf.drapi;

import org.openntf.drapi.auth.AuthenticationProvider;
import org.openntf.drapi.http.HttpTransport;
import org.openntf.drapi.json.JsonBinding;

public interface DrapiContext {

    DrapiConfig config();

    JsonBinding jsonBinding();

    HttpTransport httpTransport();

    AuthenticationProvider authenticationProvider();

}
