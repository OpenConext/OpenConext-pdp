package pdp.conflicts;

import org.junit.Test;
import pdp.domain.PdpPolicyDefinition;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static pdp.domain.PdpPolicyDefinition.policyDefinition;

public class PolicyConflictServiceTest {

    private final PolicyConflictService subject = new PolicyConflictService();

    @Test
    public void testConflicts() throws Exception {
        Map<String, List<PdpPolicyDefinition>> conflicts = subject.conflicts(asList(
                //sp equals and overlapping idp
                policyDefinition(asList("sp1"), asList("idp1", "idp2")),
                policyDefinition(asList("sp1"), asList("idp2", "idp3")),
                policyDefinition(asList("sp1"), asList("idp3")),
                //sp equals and sp2 has no Idp's
                policyDefinition(asList("sp2"), Collections.emptyList()),
                policyDefinition(asList("sp2"), asList("idp1")),
                //sp equals but no overlapping Idp
                policyDefinition(asList("sp3"), asList("idp1")),
                policyDefinition(asList("sp3"), asList("idp2")),
                //no conflicting SP
                policyDefinition(asList("sp4"), asList("idp2"))
        ));

        assertEquals(2, conflicts.size());
        assertEquals(new HashSet<>(asList("sp1", "sp2")), conflicts.keySet());

        List<PdpPolicyDefinition> sp1 = conflicts.get("sp1");
        assertEquals(3, sp1.size());
        sp1.forEach(def -> def.getServiceProviderIds().forEach(id -> assertEquals("sp1", id)));

        List<PdpPolicyDefinition> sp2 = conflicts.get("sp2");
        assertEquals(2, sp2.size());
        sp2.forEach(def -> def.getServiceProviderIds().forEach(id -> assertEquals("sp2", id)));

    }


}