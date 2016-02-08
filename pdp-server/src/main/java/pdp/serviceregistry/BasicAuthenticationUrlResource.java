package pdp.serviceregistry;

import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URLConnection;
import java.util.Base64;

public class BasicAuthenticationUrlResource extends UrlResource {

  private final String basicAuth;

  public BasicAuthenticationUrlResource(String path, String username, String password) throws MalformedURLException {
    super(path);
    this.basicAuth = "Basic " + new String(Base64.getEncoder().encode((username + ":" + password).getBytes()));
  }

  @Override
  public InputStream getInputStream() throws IOException {
    URLConnection con = this.getURL().openConnection();
    con.setRequestProperty("Authorization", basicAuth);
    con.setRequestProperty(HttpHeaders.CONTENT_TYPE, "application/json");
    con.setConnectTimeout(5 * 1000);
    try {
      return con.getInputStream();
    } catch (IOException ex) {
      if (con instanceof HttpURLConnection) {
        ((HttpURLConnection) con).disconnect();
      }
      throw ex;
    }
  }
}
