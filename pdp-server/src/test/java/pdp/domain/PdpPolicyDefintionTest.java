package pdp.domain;

import org.apache.commons.io.IOUtils;
import org.apache.openaz.xacml.util.XACMLProperties;
import org.junit.After;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class PdpPolicyDefintionTest {

  @After
  public void after() throws Exception {
    /*
     * There is only one single static instance of XACML properties and as we don't provide one here
     * other tests fail to set the properties file as the default initialization is cached
     */
    XACMLProperties.reloadProperties();
  }

  @Test
  public void fromPolicyXmlHappyFlow() throws Exception {
    String policyXml = IOUtils.toString(new ClassPathResource("SURFconext.LeidenUniv_Enquetetool.xml").getInputStream());
    String policyName = "Leiden Universiteit Enquetetool";
    PdpPolicyDefintion defintion = new PdpPolicyDefintion(policyName,policyXml);

    assertEquals(policyName, defintion.getName());
    assertEquals("Medewerkers van de lerarenopleiding in Leiden hebben toegang tot Enquetetool van Leiden.", defintion.getDescription());
    assertEquals("https://enquetetool.nl/LeidenUniv",defintion.getServiceProviderId());
    assertEquals(Arrays.asList("LeidenUniv_IDP"), defintion.getIdentityProviderIds());
    assertEquals("You are not authorized to access Enquetetool.", defintion.getDenyAdvice());
    List<PdpAttribute> attributesExpected = Arrays.asList(new PdpAttribute("faculty", "lerarenopleiding"), new PdpAttribute("urn:mace:dir:attribute-def:eduPersonAffiliation", "employee"));
    assertEquals(attributesExpected, defintion.getAttributes());
  }

}