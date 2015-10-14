package pdp.xacml;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.Assert;
import pdp.PolicyTemplateEngine;
import pdp.domain.PdpPolicy;
import pdp.repositories.PdpPolicyRepository;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.toList;

/*
 * Loads all policies in /xacml/polices into the database for testing purposes.
 *
 * This is done in 'dev' modus of the PdpApplication
 */
public class DevelopmentPrePolicyLoader implements PolicyLoader {

  private static Logger LOG = LoggerFactory.getLogger(DevelopmentPrePolicyLoader.class);

  private final Resource baseDirResource;
  private final PdpPolicyRepository pdpPolicyRepository;

  public DevelopmentPrePolicyLoader(Resource baseDirResource, PdpPolicyRepository pdpPolicyRepository) {
    this.pdpPolicyRepository = pdpPolicyRepository;
    this.baseDirResource = baseDirResource;
    Assert.isTrue(this.baseDirResource.exists());
  }

  @Override
  public List<PdpPolicy> getPolicies() {
    List<File> policyFiles;
    try {
      policyFiles = Arrays.asList(baseDirResource.getFile().listFiles((dir, name) ->
          name.endsWith("xml")));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return policyFiles.stream().map(file -> this.createPdpPolicy(getXml(file), file.getName())).collect(toList());
  }

  @Override
  public void loadPolicies() {
    pdpPolicyRepository.deleteAll();
    List<PdpPolicy> policies = getPolicies();
    policies.forEach(policy -> {
      pdpPolicyRepository.save(policy);
      LOG.info("Loaded {} policy", policy.getName());
    });
  }

  private PdpPolicy createPdpPolicy(String xml, String name) {
    xml = xml.replaceFirst("PolicyId=\".*\"", "PolicyId=\"" + PolicyTemplateEngine.getPolicyId(name) + "\"");
    return new PdpPolicy(xml, name);
  }

  private String getXml(File file) {
    try {
      return IOUtils.toString(new FileInputStream(file));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

}
