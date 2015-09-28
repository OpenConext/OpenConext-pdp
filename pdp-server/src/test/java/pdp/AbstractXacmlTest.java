package pdp;

import org.apache.openaz.xacml.util.XACMLProperties;
import org.junit.After;

public abstract class AbstractXacmlTest {

  @After
  public void after() throws Exception {
    /*
     * There is only one single static instance of XACML properties and as we don't provide one here
     * other tests fail to set the properties file as the default initialization is cached
     */
    XACMLProperties.reloadProperties();
  }

}
