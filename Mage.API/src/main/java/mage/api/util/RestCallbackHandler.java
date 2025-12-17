package mage.api.util;

import org.jboss.remoting.callback.AsynchInvokerCallbackHandler;
import org.jboss.remoting.callback.Callback;
import org.jboss.remoting.callback.HandleCallbackException;

/**
 * Dummy callback handler for REST API.
 * In REST, we don't use push callbacks - clients poll for updates instead.
 */
public class RestCallbackHandler implements AsynchInvokerCallbackHandler {

    @Override
    public void handleCallback(Callback callback) throws HandleCallbackException {
        // No-op for REST API - clients will poll for updates
    }

    @Override
    public void handleCallbackOneway(Callback callback) throws HandleCallbackException {
        // No-op for REST API - clients will poll for updates
    }

    @Override
    public void handleCallbackOneway(Callback callback, boolean async) throws HandleCallbackException {
        // No-op for REST API - clients will poll for updates
    }

    @Override
    public void handleCallback(Callback callback, boolean async, boolean oneway) throws HandleCallbackException {
        // No-op for REST API - clients will poll for updates
    }

    public String getClientSessionId() {
        return null;
    }
}
