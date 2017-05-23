package pdp.domain;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import pdp.JsonMapper;
import pdp.policies.PolicyLoader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class PdpPolicyTest implements JsonMapper {

    @Test
    public void testRevision() throws Exception {
        PdpPolicy parent = new PdpPolicy();

        PdpPolicy revision = PdpPolicy.revision("new policy", parent, "xml", "uid", PolicyLoader.authenticatingAuthority, "John Doe", true);

        assertFalse(parent.isLatestRevision());
        assertEquals(1, parent.getRevisions().size());
        assertEquals(revision, parent.getRevisions().iterator().next());
        assertEquals(0, parent.getRevisionNbr());

        assertTrue(revision.isLatestRevision());
        assertEquals(1, revision.getRevisionNbr());
    }

    @Test
    public void testCreateReadonly() throws Exception {
        String json = IOUtils.toString(new ClassPathResource("policies/update_policy.json").getInputStream());
        PdpPolicyDefinition definition = objectMapper.readValue(json, PdpPolicyDefinition.class);

        assertNull(definition.getCreated());

    }
}