package pdp;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.util.Arrays;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = PdpApplication.class)
@WebIntegrationTest(randomPort = true, value = {"xacml.properties.path=classpath:xacml.conext.test.database.properties"})
public class PdpApplicationDatabaseTest extends PdpApplicationTest {

  @Autowired
  private PdpPolicyRepository repository;

  @Before
  public void before() throws IOException {
    super.before();
    repository.deleteAll();
    repository.save(Arrays.asList(
        new PdpPolicy(IOUtils.toString(new ClassPathResource("SURFconext.SURFspotAccess.xml").getInputStream())),
        new PdpPolicy(IOUtils.toString(new ClassPathResource("SURFconext.TeamAccess.xml").getInputStream()))));
  }

}
