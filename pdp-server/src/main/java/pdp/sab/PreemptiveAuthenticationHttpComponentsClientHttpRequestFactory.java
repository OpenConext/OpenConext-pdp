package pdp.sab;

import org.apache.http.HttpHost;
import org.apache.http.client.AuthCache;
import org.apache.http.client.HttpClient;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

/**
 * Default HttpClient does not support Preemptive authentication. Spring has added a hook to
 * support this: https://jira.spring.io/browse/SPR-8367
 */
public class PreemptiveAuthenticationHttpComponentsClientHttpRequestFactory extends HttpComponentsClientHttpRequestFactory {

    private HttpContext httpContext;

    public PreemptiveAuthenticationHttpComponentsClientHttpRequestFactory(HttpClient httpClient, String url) throws MalformedURLException {
        super(httpClient);
        this.httpContext = this.initHttpContext(url);
    }

    private HttpContext initHttpContext(String url) throws MalformedURLException {
        URL parsedUrl = new URL(url);
        HttpHost targetHost = new HttpHost(parsedUrl.getHost(), parsedUrl.getPort(), parsedUrl.getProtocol());
        AuthCache authCache = new BasicAuthCache();
        BasicScheme basicAuth = new BasicScheme();
        authCache.put(targetHost, basicAuth);
        BasicHttpContext localContext = new BasicHttpContext();
        localContext.setAttribute(HttpClientContext.AUTH_CACHE, authCache);
        return localContext;
    }

    @Override
    protected HttpContext createHttpContext(HttpMethod httpMethod, URI uri) {
        return this.httpContext;
    }
}
