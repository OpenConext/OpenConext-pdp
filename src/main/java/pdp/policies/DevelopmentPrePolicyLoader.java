package pdp.policies;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import pdp.domain.PdpPolicy;
import pdp.repositories.PdpPolicyRepository;
import pdp.repositories.PdpPolicyViolationRepository;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static pdp.xacml.PolicyTemplateEngine.getPolicyId;

/*
 * Loads all policies in /xacml/policies into the database for testing purposes.
 *
 * This is done in 'dev' modus of the PdpApplication
 */
public class DevelopmentPrePolicyLoader implements PolicyLoader {

    private static Logger LOG = LoggerFactory.getLogger(DevelopmentPrePolicyLoader.class);

    private final Resource baseDirResource;
    private final PdpPolicyRepository pdpPolicyRepository;
    private final PdpPolicyViolationRepository pdpPolicyViolationRepository;

    public DevelopmentPrePolicyLoader(Resource baseDirResource,
                                      PdpPolicyRepository pdpPolicyRepository,
                                      PdpPolicyViolationRepository pdpPolicyViolationRepository) {
        this.pdpPolicyRepository = pdpPolicyRepository;
        this.pdpPolicyViolationRepository = pdpPolicyViolationRepository;
        this.baseDirResource = baseDirResource;
        Assert.isTrue(this.baseDirResource.exists(), "Basedir resource must exist: " + baseDirResource);
    }

    @Override
    public List<PdpPolicy> getPolicies() throws IOException {
        return Arrays.stream(baseDirResource.getFile().listFiles((dir, name) -> name.endsWith("xml")))
            .map(file -> this.createPdpPolicy(getXml(file), file.getName())).collect(toList());
    }

    @Override
    public void loadPolicies() throws IOException {
        pdpPolicyViolationRepository.deleteAll();
        pdpPolicyRepository.deleteAll();

        getPolicies().forEach(policy -> {
            pdpPolicyRepository.save(policy);
            LOG.info("Loaded {} policy", policy.getName());
        });
    }

    public String fileNameToPolicyName(String fileName) {
        return fileName.replaceAll("(\\.|xml)", "").replaceAll("([A-Z])", " $1").toLowerCase();
    }

    private PdpPolicy createPdpPolicy(String xml, String fileName) {
        String name = fileNameToPolicyName(fileName).trim();
        xml = xml.replaceFirst("PolicyId=\".*\"", "PolicyId=\"" + getPolicyId(name) + "\"");
        return new PdpPolicy(xml, name, true, userIdentifier, authenticatingAuthority, userDisplayName, true, "reg");
    }

    private String getXml(File file) {
        try {
            return IOUtils.toString(new FileInputStream(file), Charset.defaultCharset());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
