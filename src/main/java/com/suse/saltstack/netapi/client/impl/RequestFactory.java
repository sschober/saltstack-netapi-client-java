package com.suse.saltstack.netapi.client.impl;

import com.suse.saltstack.netapi.config.ClientConfig;
import static com.suse.saltstack.netapi.config.ClientConfig.*;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;

import javax.xml.bind.DatatypeConverter;

/**
 * Helper class for setting up {@link HttpURLConnection} objects.
 */
public class RequestFactory {

    /** Singleton instance. */
    private static RequestFactory instance = new RequestFactory();

    /**
     * Instantiates a new SaltStack request factory.
     */
    private RequestFactory() {
    }

    /**
     * Gets the single instance of {@link RequestFactory}.
     * @return single instance of {@link RequestFactory}
     */
    public static RequestFactory getInstance() {
        return instance;
    }

    /**
     * Init a {@link HttpURLConnection} object from a given URI.
     *
     * @param method the method
     * @param endpoint the endpoint
     * @param config the config
     * @return connection
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public HttpURLConnection initConnection(String method, String endpoint,
            ClientConfig config) throws IOException {
        // Init the connection
        URL url = config.get(URL).resolve(endpoint).toURL();
        HttpURLConnection connection;

        // Optionally connect via a given proxy
        String proxyHost = config.get(PROXY_HOSTNAME);
        if (proxyHost != null) {
            int proxyPort = config.get(PROXY_PORT);
            Proxy proxy = new Proxy(Proxy.Type.HTTP,
                    new InetSocketAddress(proxyHost, proxyPort));
            connection = (HttpURLConnection) url.openConnection(proxy);

            // Proxy authentication
            String proxyUsername = config.get(PROXY_USERNAME);
            String proxyPassword = config.get(PROXY_PASSWORD);
            if (proxyUsername != null && proxyPassword != null) {
                final String encoded = DatatypeConverter.printBase64Binary(
                        (proxyUsername + ':' + proxyPassword).getBytes());
                connection.addRequestProperty("Proxy-Authorization", encoded);
            }
        } else {
            connection = (HttpURLConnection) url.openConnection();
        }

        // Configure the connection before returning it
        connection.setRequestMethod(method);
        connection.setRequestProperty("Accept", "application/json");

        // Token authentication
        String token = config.get(TOKEN);
        if (token != null) {
            connection.setRequestProperty("X-Auth-Token", token);
        }

        return connection;
    }
}
