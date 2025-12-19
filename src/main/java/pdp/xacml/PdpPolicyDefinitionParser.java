package pdp.xacml;

import org.apache.openaz.xacml.pdp.policy.*;
import org.apache.openaz.xacml.pdp.policy.dom.DOMPolicyDef;
import org.apache.openaz.xacml.std.dom.DOMStructureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;

/*
 * Thread-safe
 */
public class PdpPolicyDefinitionParser  {

    private static final Logger LOG = LoggerFactory.getLogger(PdpPolicyDefinitionParser.class);

    public static final String NAME_ID = "urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified";
    public static final String UID = "urn:mace:dir:attribute-def:uid";
    public static final String SCHAC_HOME_ORGANIZATION = "urn:mace:terena.org:attribute-def:schacHomeOrganization";
    public static final String IP_FUNCTION = "urn:surfnet:cbac:custom:function:3.0:ip:range";
    public static final String NEGATE_FUNCTION = "urn:surfnet:cbac:custom:function:3.0:negation";
    public static final String BAG_FUNCTION = "urn:surfnet:cbac:custom:function:3.0:bag";

    public Policy parsePolicy(String policyXml, String name) {
        String cleanedXml = policyXml.trim().replaceAll("\n", "").replaceAll(" +", " ");
        try {
            return (Policy) DOMPolicyDef.load(new ByteArrayInputStream(cleanedXml.getBytes()));
        } catch (DOMStructureException e) {
            LOG.warn(String.format("Failed to parse %s policyXml: %s", name, policyXml), e);
            throw new RuntimeException(e);
        }
    }

}
