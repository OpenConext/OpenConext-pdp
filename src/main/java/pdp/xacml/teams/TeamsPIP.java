package pdp.xacml.teams;

import org.apache.openaz.xacml.api.Attribute;
import org.apache.openaz.xacml.api.AttributeValue;
import org.apache.openaz.xacml.api.pip.PIPException;
import org.apache.openaz.xacml.api.pip.PIPFinder;
import org.apache.openaz.xacml.api.pip.PIPRequest;
import org.apache.openaz.xacml.api.pip.PIPResponse;
import org.apache.openaz.xacml.std.*;
import org.apache.openaz.xacml.std.pip.StdMutablePIPResponse;
import org.apache.openaz.xacml.std.pip.StdPIPRequest;
import org.apache.openaz.xacml.std.pip.StdSinglePIPResponse;
import org.apache.openaz.xacml.std.pip.engines.ConfigurableEngine;
import org.springframework.util.CollectionUtils;

import java.util.*;

public class TeamsPIP implements ConfigurableEngine {

  private PIPRequest requiredAttribute;
  private PIPRequest providedAttribute;

  private PIPResponse empty;
  private PIPResponse missingNameId;

  private String groupNameUri = "urn:mace:dir:attribute-def:group-name";

  @Override
  public void configure(String id, Properties properties) throws PIPException {
    IdentifierImpl identifierDataType = new IdentifierImpl("http://www.w3.org/2001/XMLSchema#string");

    requiredAttribute = new StdPIPRequest(
        // identifierCategory
        new IdentifierImpl("urn:oasis:names:tc:xacml:1.0:subject-category:access-subject"),
        // identifierAttribute
        new IdentifierImpl("urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified"),
        // identifierDataType
        identifierDataType);


    IdentifierImpl attributeCategory = new IdentifierImpl("urn:oasis:names:tc:xacml:3.0:attribute-category:resource");
    IdentifierImpl identifierAttribute = new IdentifierImpl("urn:mace:dir:attribute-def:group-name");

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

    missingNameId = new StdMutablePIPResponse(new StdStatus(StdStatusCode.STATUS_CODE_MISSING_ATTRIBUTE, "urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified attribute missing"));
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
    if (!this.groupNameUri.equals(pipRequest.getAttributeId().getUri().toString())) {
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
    String name = (String) values.stream().findFirst().get().getValue();
    if ("mary.doe".equals(name)) {
      StdAttributeValue<String> sss = new StdAttributeValue(providedAttribute.getDataTypeId(), "xacml-admins");
      Collection<AttributeValue<?>> stdAttributeValues = Arrays.asList(sss);
      Attribute responseAttr = new StdAttribute(providedAttribute.getCategory(), providedAttribute.getAttributeId(), stdAttributeValues, null, true);
      return new StdSinglePIPResponse(responseAttr);
    } else {
      return empty;
    }
  }
}
