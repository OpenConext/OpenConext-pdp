package pdp.serviceregistry;

import org.springframework.core.io.UrlResource;
import org.springframework.util.ResourceUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URLConnection;
import java.util.Base64;

public class AuthorizationURLResource extends UrlResource {

  private final String userName;
  private final String password;

  public AuthorizationURLResource(String path, String userName, String password) throws MalformedURLException {
    super(path);
    this.userName = userName;
    this.password = password;
  }

  @Override
  public InputStream getInputStream() throws IOException {
    URLConnection con = this.getURLConnection();
    ResourceUtils.useCachesIfNecessary(con);
    try {
      return con.getInputStream();
    }
    catch (IOException ex) {
      // Close the HTTP connection (if applicable).
      if (con instanceof HttpURLConnection) {
        ((HttpURLConnection) con).disconnect();
      }
      throw ex;
    }
  }

  private URLConnection getURLConnection() throws IOException {
    URLConnection urlConnection = getURL().openConnection();
    String userpass = userName + ":" + password;
    String basicAuth = "Basic " + new String(Base64.getEncoder().encodeToString(userpass.getBytes()));
    urlConnection.setRequestProperty ("Authorization", basicAuth);
    return urlConnection;
  }
}
