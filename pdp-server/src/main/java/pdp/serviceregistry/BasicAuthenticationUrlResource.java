package pdp.serviceregistry;

import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URLConnection;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Base64;

import static java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME;
import static org.springframework.http.HttpHeaders.IF_MODIFIED_SINCE;

public class BasicAuthenticationUrlResource extends UrlResource {

    private final String basicAuth;

    public BasicAuthenticationUrlResource(String path, String username, String password) throws MalformedURLException {
        super(path);
        this.basicAuth = "Basic " + new String(Base64.getEncoder().encode((username + ":" + password).getBytes()));
    }

    @Override
    public InputStream getInputStream() throws IOException {
        HttpURLConnection con = (HttpURLConnection) this.getURL().openConnection();
        setHeaders(con);
        try {
            return con.getInputStream();
        } catch (IOException ex) {
            con.disconnect();
            throw ex;
        }
    }

    public boolean isModified(int minutes) {
        HttpURLConnection con = null;
        try {
            con = (HttpURLConnection) this.getURL().openConnection();
            con.setRequestMethod("HEAD");
            setHeaders(con);

            String lastRefresh = RFC_1123_DATE_TIME.format(ZonedDateTime.now(ZoneId.of("GMT")).minusMinutes(minutes));
            con.setRequestProperty(IF_MODIFIED_SINCE, lastRefresh);

            int responseCode = con.getResponseCode();
            return responseCode != HttpStatus.NOT_MODIFIED.value();
        } catch (IOException ex) {
            con.disconnect();
            throw new RuntimeException(ex);
        }
    }

    private void setHeaders(URLConnection con) {
        con.setRequestProperty("Authorization", basicAuth);
        con.setRequestProperty(HttpHeaders.CONTENT_TYPE, "application/json");
        con.setConnectTimeout(5 * 1000);
    }


}
