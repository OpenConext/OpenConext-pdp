package pdp.domain;

import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.*;
import static pdp.xacml.PolicyTemplateEngine.getPolicyId;

public class PdpPolicyTest {

  @Test
  public void testRevision() throws Exception {
    PdpPolicy parent = new PdpPolicy();
    PdpPolicyDefinition pdpPolicyDefinition = new PdpPolicyDefinition();
    pdpPolicyDefinition.setName("new policy");

    PdpPolicy revision = PdpPolicy.revision(pdpPolicyDefinition, parent, "xml", "uid", "http://mock-idp", "John Doe");

    assertFalse(parent.isLatestRevision());
    assertEquals(1, parent.getRevisions().size());
    assertEquals(revision, parent.getRevisions().iterator().next());
    assertEquals(0, parent.getRevisionNbr());

    assertTrue(revision.isLatestRevision());
    assertEquals(1, revision.getRevisionNbr());
  }
}