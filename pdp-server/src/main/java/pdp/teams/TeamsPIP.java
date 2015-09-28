package pdp.teams;

import org.apache.openaz.xacml.api.Attribute;
import org.apache.openaz.xacml.api.AttributeValue;
import org.apache.openaz.xacml.api.Identifier;
import org.apache.openaz.xacml.api.pip.PIPException;
import org.apache.openaz.xacml.api.pip.PIPFinder;
import org.apache.openaz.xacml.api.pip.PIPRequest;
import org.apache.openaz.xacml.api.pip.PIPResponse;
import org.apache.openaz.xacml.std.IdentifierImpl;
import org.apache.openaz.xacml.std.StdAttribute;
import org.apache.openaz.xacml.std.StdAttributeValue;
import org.apache.openaz.xacml.std.pip.StdPIPRequest;
import org.apache.openaz.xacml.std.pip.StdSinglePIPResponse;
import org.apache.openaz.xacml.std.pip.engines.ConfigurableEngine;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

import static pdp.xacml.PdpPolicyDefinitionParser.GROUP_URN;
import static pdp.xacml.PdpPolicyDefinitionParser.NAME_ID;

public class TeamsPIP implements ConfigurableEngine, VootClientAware {

    private VootClient vootClient;

    private PIPRequest requiredAttribute;
    private PIPRequest providedAttribute;

    private PIPResponse empty;

    @Override
    public void configure(String id, Properties properties) throws PIPException {
        IdentifierImpl identifierDataType = new IdentifierImpl("http://www.w3.org/2001/XMLSchema#string");

        requiredAttribute = new StdPIPRequest(
                // identifierCategory
                new IdentifierImpl("urn:oasis:names:tc:xacml:1.0:subject-category:access-subject"),
                // identifierAttribute
                new IdentifierImpl(NAME_ID),
                // identifierDataType
                identifierDataType);


        IdentifierImpl attributeCategory = new IdentifierImpl("urn:oasis:names:tc:xacml:3.0:attribute-category:resource");
        IdentifierImpl identifierAttribute = new IdentifierImpl(GROUP_URN);

        providedAttribute = new StdPIPRequest(
                // identifierCategory
                attributeCategory,
                // identifierAttribute
                identifierAttribute,
                // identifierDataType
                identifierDataType);

        Attribute attribute = new StdAttribute(attributeCategory,
                identifierAttribute,
                Collections.EMPTY_LIST, null, true);
        empty = new StdSinglePIPResponse(attribute);

    }

    @Override
    public String getName() {
        return "teams_pip";
    }

    @Override
    public String getDescription() {
        return "Teams Policy Information Point";
    }

    @Override
    public Collection<PIPRequest> attributesRequired() {
        return Arrays.asList(requiredAttribute);
    }

    @Override
    public Collection<PIPRequest> attributesProvided() {
        return Arrays.asList(providedAttribute);
    }

    @Override
    public PIPResponse getAttributes(PIPRequest pipRequest, PIPFinder pipFinder) throws PIPException {
        if (!GROUP_URN.equals(pipRequest.getAttributeId().getUri().toString())) {
            //this PIP requires a groupName rule to be present in the Policy
            return empty;
        }
        PIPResponse matchingAttributes = pipFinder.getMatchingAttributes(requiredAttribute, this);
        Optional<Attribute> nameAttributeOptional = matchingAttributes.getAttributes().stream().findFirst();
        if (!nameAttributeOptional.isPresent()) {
            return empty;
        }
        Attribute nameAttribute = nameAttributeOptional.get();
        Collection<AttributeValue<?>> values = nameAttribute.getValues();
        if (CollectionUtils.isEmpty(values)) {
            return empty;
        }
        String userUrn = (String) values.stream().findFirst().get().getValue();
        List<String> groups = vootClient.groups(userUrn);
        Identifier groupNameDataTypeId = providedAttribute.getDataTypeId();
        List<AttributeValue<?>> stdAttributeValues = groups.stream().map(group -> new StdAttributeValue<>(groupNameDataTypeId, group)).collect(Collectors.toList());
        Attribute responseAttr = new StdAttribute(providedAttribute.getCategory(), providedAttribute.getAttributeId(), stdAttributeValues, null, true);
        return new StdSinglePIPResponse(responseAttr);
    }

    public void setVootClient(VootClient vootClient) {
        this.vootClient = vootClient;
    }
}
