package pdp.web;

import lombok.SneakyThrows;
import org.apache.hc.core5.http.HttpHost;
import org.junit.Test;

import java.net.URL;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class HttpHostProviderTest {

    @Test
    public void resolveHttpHost() {
        System.setProperty("http.proxyHost", "proxy.com");
        System.setProperty("http.nonProxyHosts", "*.example.com|localhost|*domain.org|*broadband*");
        boolean ignoreProxy = Stream.of(
                        "https://www.example.com",
                        "https://www.sub.example.com",
                        "http://localhost:8080",
                        "https://sub.domain.org",
                        "https://www.broadband.com")
                .map(url -> HttpHostProvider.resolveHttpHost(url(url)))
                .noneMatch(Optional::isPresent);
        assertTrue(ignoreProxy);
    }

    @Test
    public void resolveHttpHostWithEmptyProxyHost() {
        System.setProperty("http.proxyHost", " ");
        assertTrue(HttpHostProvider.resolveHttpHost(url("https://example.com:443/test")).isEmpty());
    }

    @Test
    public void resolveHttpHostWithDefaultPort() {
        System.setProperty("http.proxyHost", "localhost");
        HttpHost httpHost = HttpHostProvider.resolveHttpHost(url("https://example.com:443/test")).get();
        assertEquals("localhost:80", String.format("%s:%s", httpHost.getHostName(), httpHost.getPort()));
    }

    @Test
    public void resolveHttpHostWithSpecifiedPort() {
        System.setProperty("http.proxyHost", "localhost");
        System.setProperty("http.proxyPort", "8080");
        HttpHost httpHost = HttpHostProvider.resolveHttpHost(url("https://example.com:443/test")).get();
        assertEquals("localhost:8080", String.format("%s:%s", httpHost.getHostName(), httpHost.getPort()));
    }

    @SneakyThrows
    private URL url(String url) {
        return new URL(url);
    }
}
