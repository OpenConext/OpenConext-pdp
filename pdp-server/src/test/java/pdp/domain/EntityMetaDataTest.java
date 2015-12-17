package pdp.domain;

import org.junit.Before;
import org.junit.Test;
import pdp.policies.PolicyLoader;

import java.util.Arrays;
import java.util.HashSet;

import static java.util.Arrays.asList;
import static org.junit.Assert.*;

public class EntityMetaDataTest {

  private EntityMetaData metaData;

  @Before
  public void before() {
    metaData = new EntityMetaData(PolicyLoader.authenticatingAuthority, "institutionId", "descriptionEn", "nameEn", "descriptionNl",
        "nameNl", true, false, new HashSet(asList("http://mock-sp", "http://dummy-sp")));
  }

  @Test
  public void testIsAllowedFrom() throws Exception {
    assertFalse(metaData.isAllowedFrom());
    assertFalse(metaData.isAllowedFrom("http://private-sp", "http://unknown-sp"));

    assertTrue(metaData.isAllowedFrom("http://mock-sp"));

    //only one match is required
    assertTrue(metaData.isAllowedFrom("http://mock-sp", "http://private-sp"));
  }
}