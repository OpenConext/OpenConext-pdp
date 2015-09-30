package pdp.xacml;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
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
public class DevelopmentPrePolicyLoader {

  private static Logger LOG = LoggerFactory.getLogger(DevelopmentPrePolicyLoader.class);

  private final Resource resource = new ClassPathResource("/xacml/policies");

  public List<PdpPolicy> getPolicies() {
    List<File> policyFiles;
    try {
      policyFiles = Arrays.asList(resource.getFile().listFiles((dir, name) ->
          name.endsWith("xml")));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return policyFiles.stream().map(file -> this.createPdpPolicy(file)).collect(toList());
  }

  public void loadPolicies(PdpPolicyRepository pdpPolicyRepository, boolean clearDatabase) {
    if (clearDatabase) {
      pdpPolicyRepository.deleteAll();
    }
    List<PdpPolicy> policies = getPolicies();
    policies.forEach(policy -> {
      if (!clearDatabase) {
        this.deletePolicyIfExists(policy, pdpPolicyRepository);
      }
      pdpPolicyRepository.save(policy);
      LOG.info("Loaded {} policy from {}", policy.getName(), resource.getFilename());
    });
  }

  private void deletePolicyIfExists(PdpPolicy policy, PdpPolicyRepository pdpPolicyRepository) {
    List<PdpPolicy> byName = pdpPolicyRepository.findByName(policy.getName());
    if (!byName.isEmpty()) {
      pdpPolicyRepository.delete(byName);
    }
  }

  private PdpPolicy createPdpPolicy(File file) {
    try {
      String xml = IOUtils.toString(new FileInputStream(file));
      return new PdpPolicy(xml, file.getName());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
