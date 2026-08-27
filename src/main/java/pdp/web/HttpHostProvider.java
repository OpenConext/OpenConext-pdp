package pdp.web;

import org.apache.hc.core5.http.HttpHost;
import org.springframework.util.StringUtils;

import java.net.URL;
import java.util.Optional;
import java.util.stream.Stream;

import static org.apache.commons.io.FilenameUtils.wildcardMatch;

public class HttpHostProvider {

    private HttpHostProvider() {
    }

    public static Optional<HttpHost> resolveHttpHost(URL url) {
        String proxyHost = System.getProperty("http.proxyHost");
        String nonProxyHosts = System.getProperty("http.nonProxyHosts");
        boolean ignoreProxy = false;

        if (StringUtils.hasText(nonProxyHosts)) {
            String host = url.getHost();
            ignoreProxy = Stream.of(nonProxyHosts.split("\\|"))
                    .map(String::trim)
                    .anyMatch(nonProxyHost -> wildcardMatch(host, nonProxyHost));
        }

        if (StringUtils.hasText(proxyHost) && !ignoreProxy) {
            String proxyPortString = System.getProperty("http.proxyPort");
            //Default port is 80. See https://docs.oracle.com/javase/8/docs/technotes/guides/net/proxies.html
            int proxyPort = StringUtils.hasText(proxyPortString) ? Integer.parseInt(proxyPortString) : 80;
            return Optional.of(new HttpHost(proxyHost, proxyPort));

        }
        return Optional.empty();
    }
}
