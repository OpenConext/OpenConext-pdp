package pdp.teams;

import org.apache.openaz.xacml.api.Attribute;
import org.apache.openaz.xacml.api.AttributeValue;
import org.apache.openaz.xacml.api.Identifier;
import org.apache.openaz.xacml.api.pip.PIPException;
import org.apache.openaz.xacml.api.pip.PIPFinder;
import org.apache.openaz.xacml.api.pip.PIPRequest;
import org.apache.openaz.xacml.api.pip.PIPResponse;
import org.apache.openaz.xacml.std.*;
import org.apache.openaz.xacml.std.pip.StdMutablePIPResponse;
import org.apache.openaz.xacml.std.pip.StdPIPRequest;
import org.apache.openaz.xacml.std.pip.StdSinglePIPResponse;
import org.apache.openaz.xacml.std.pip.engines.ConfigurableEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.util.*;

import static java.util.stream.Collectors.toList;
import static pdp.xacml.PdpPolicyDefinitionParser.GROUP_URN;
import static pdp.xacml.PdpPolicyDefinitionParser.NAME_ID;

public class TeamsPIP implements ConfigurableEngine, VootClientAware {

  private final static Logger LOG = LoggerFactory.getLogger(TeamsPIP.class);

  private VootClient vootClient;

  private PIPRequest requiredAttribute;
  private PIPRequest providedAttribute;

  private PIPResponse empty;
  private PIPResponse missingNameId;

  @Override
  public void configure(String id, Properties properties) throws PIPException {
    IdentifierImpl identifierDataType = new IdentifierImpl("http://www.w3.org/2001/XMLSchema#string");
    IdentifierImpl attributeCategory = new IdentifierImpl("urn:oasis:names:tc:xacml:1.0:subject-category:access-subject");

    requiredAttribute = new StdPIPRequest(attributeCategory, new IdentifierImpl(NAME_ID), identifierDataType);

    IdentifierImpl identifierAttribute = new IdentifierImpl(GROUP_URN);
    providedAttribute = new StdPIPRequest(attributeCategory, identifierAttribute, identifierDataType);

    Attribute attribute = new StdAttribute(attributeCategory, identifierAttribute, Collections.EMPTY_LIST, null, true);
    empty = new StdSinglePIPResponse(attribute);
    missingNameId = new StdMutablePIPResponse(new StdStatus(StdStatusCode.STATUS_CODE_MISSING_ATTRIBUTE, NAME_ID + " attribute missing"));

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
      return missingNameId;
    }
    Attribute nameAttribute = nameAttributeOptional.get();
    Collection<AttributeValue<?>> values = nameAttribute.getValues();
    if (CollectionUtils.isEmpty(values)) {
      return missingNameId;
    }
    String userUrn = (String) values.stream().findFirst().get().getValue();
    List<String> groups = vootClient.groups(userUrn);
    if (CollectionUtils.isEmpty(groups)) {
      return empty;
    }
    Identifier groupNameDataTypeId = providedAttribute.getDataTypeId();
    List<AttributeValue<?>> stdAttributeValues = groups.stream().map(group -> new StdAttributeValue<>(groupNameDataTypeId, group)).collect(toList());
    Attribute responseAttr = new StdAttribute(providedAttribute.getCategory(), providedAttribute.getAttributeId(), stdAttributeValues, null, true);
    LOG.debug("Returning groups from PIP: {}", groups);
    return new StdSinglePIPResponse(responseAttr);
  }

  public void setVootClient(VootClient vootClient) {
    this.vootClient = vootClient;
  }
}
