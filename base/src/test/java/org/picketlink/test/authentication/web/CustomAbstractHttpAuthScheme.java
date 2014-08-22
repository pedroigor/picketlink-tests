package org.picketlink.test.authentication.web;

import org.picketlink.Identity;
import org.picketlink.config.http.AuthenticationSchemeConfiguration;
import org.picketlink.credential.DefaultLoginCredentials;
import org.picketlink.http.authentication.HttpAuthenticationScheme;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public abstract class CustomAbstractHttpAuthScheme implements HttpAuthenticationScheme {

    private boolean hasBeenInitialized;
    private AuthenticationSchemeConfiguration config;

    @Inject
    private Identity identity;


    @Override
    public void initialize(AuthenticationSchemeConfiguration config) {
        this.config = config;
        hasBeenInitialized = true;
    }

    @Override
    public void extractCredential(HttpServletRequest request, DefaultLoginCredentials creds) {
        // these are the credentials considered valid by this testing setup
        creds.setUserId("john");
        creds.setPassword("passwd");
    }

    @Override
    public void challengeClient(HttpServletRequest request, HttpServletResponse response) {
        response.setContentType("text/plain");
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);

        try {
            response.getOutputStream().print("this is a client challenge response");
            response.flushBuffer();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onPostAuthentication(HttpServletRequest request, HttpServletResponse response) {
        StringBuilder resp = new StringBuilder();
        resp.append(getClass().getName());
        if (hasBeenInitialized) {
            resp.append(", initialized");
        }
        if (config != null) {
            resp.append(", has_filter_config");
        }
        if (identity != null) {
            resp.append(", has_injected_identity");
        }
        response.setContentType("text/plain");

        try {
            response.getOutputStream().print(resp.toString());
            response.flushBuffer();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
