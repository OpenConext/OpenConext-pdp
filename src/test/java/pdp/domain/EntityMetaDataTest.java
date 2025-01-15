package pdp.domain;

import com.google.common.collect.ImmutableSet;
import org.junit.Test;
import pdp.policies.PolicyLoader;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class EntityMetaDataTest {

    private EntityMetaData metaData = new EntityMetaData(
            PolicyLoader.authenticatingAuthority,
            "institutionId",
            "nameEn",
            "nameNl",
            "organisationNameEn",
            "organisationNameNl",
            true,
            false,
            ImmutableSet.of("http://mock-sp", "http://dummy-sp"));

    @Test
    public void testIsAllowedFrom() throws Exception {
        assertFalse(metaData.isAllowedFrom());
        assertFalse(metaData.isAllowedFrom("http://private-sp", "http://unknown-sp"));

        assertTrue(metaData.isAllowedFrom("http://mock-sp"));

        //only one match is required
        assertTrue(metaData.isAllowedFrom("http://mock-sp", "http://private-sp"));
    }
}