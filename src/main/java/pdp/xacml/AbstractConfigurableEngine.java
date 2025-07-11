package pdp.xacml;

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
import org.apache.openaz.xacml.std.StdStatus;
import org.apache.openaz.xacml.std.StdStatusCode;
import org.apache.openaz.xacml.std.pip.StdMutablePIPResponse;
import org.apache.openaz.xacml.std.pip.StdPIPRequest;
import org.apache.openaz.xacml.std.pip.StdSinglePIPResponse;
import org.apache.openaz.xacml.std.pip.engines.ConfigurableEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import pdp.stats.StatsContext;
import pdp.stats.StatsContextHolder;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import static java.util.stream.Collectors.toList;
import static pdp.xacml.PdpPolicyDefinitionParser.*;

public abstract class AbstractConfigurableEngine implements ConfigurableEngine {

    private final Logger LOG = LoggerFactory.getLogger(getClass());

    protected PIPRequest requiredAttributeUnspecifiedURN;
    protected PIPRequest requiredAttributeSchacHome;
    protected PIPRequest requiredAttributeUID;
    protected PIPRequest providedAttribute;

    protected PIPResponse empty;
    protected PIPResponse missingNameId;

    //we need to keep track of performance

    @Override
    public void configure(String id, Properties properties) throws PIPException {
        IdentifierImpl identifierDataType = new IdentifierImpl("http://www.w3.org/2001/XMLSchema#string");
        IdentifierImpl attributeCategory = new IdentifierImpl("urn:oasis:names:tc:xacml:1.0:subject-category:access-subject");

        requiredAttributeUnspecifiedURN = new StdPIPRequest(attributeCategory, new IdentifierImpl(NAME_ID), identifierDataType);
        requiredAttributeSchacHome = new StdPIPRequest(attributeCategory, new IdentifierImpl(SCHAC_HOME_ORGANIZATION), identifierDataType);
        requiredAttributeUID = new StdPIPRequest(attributeCategory, new IdentifierImpl(UID), identifierDataType);

        IdentifierImpl identifierAttribute = new IdentifierImpl(getIdentifierProvidedAttribute());
        providedAttribute = new StdPIPRequest(attributeCategory, identifierAttribute, identifierDataType);

        Attribute attribute = new StdAttribute(attributeCategory, identifierAttribute, Collections.emptyList(), null, true);
        empty = new StdSinglePIPResponse(attribute);
        missingNameId = new StdMutablePIPResponse(new StdStatus(StdStatusCode.STATUS_CODE_MISSING_ATTRIBUTE, NAME_ID + " attribute missing"));
    }

    @Override
    public Collection<PIPRequest> attributesRequired() {
        if (useUnspecifiedURN()) {
            return List.of(requiredAttributeUnspecifiedURN);
        }
        return List.of(requiredAttributeSchacHome, requiredAttributeUID);
    }

    @Override
    public Collection<PIPRequest> attributesProvided() {
        return Arrays.asList(providedAttribute);
    }

    @Override
    public PIPResponse getAttributes(PIPRequest pipRequest, PIPFinder pipFinder) throws PIPException {
        if (!getIdentifierProvidedAttribute().equals(pipRequest.getAttributeId().getUri().toString())) {
            //this PIP requires a PIP dependent rule to be present in the Policy
            return empty;
        }
        String userUrn;
        if (useUnspecifiedURN()) {
            Optional<String> userUrnOptional = getAttribute(requiredAttributeUnspecifiedURN, pipFinder);
            if (!userUrnOptional.isPresent()) {
                return missingNameId;
            }
            userUrn = userUrnOptional.get();
        } else {
            Optional<String> schacHomeOptional = getAttribute(requiredAttributeSchacHome, pipFinder);
            if (!schacHomeOptional.isPresent()) {
                return missingNameId;
            }
            Optional<String> uidOptional = getAttribute(requiredAttributeUID, pipFinder);
            if (!uidOptional.isPresent()) {
                return missingNameId;
            }
            //Seems hackey, but the alternative is to have no common code between SAB and Teams PIP
            userUrn = String.format("urn:collab:person:%s:%s", schacHomeOptional.get(), uidOptional.get());
        }
        StatsContext stats = StatsContextHolder.getContext();
        long start = System.currentTimeMillis();

        List<Object> result = getAttributes(userUrn);

        long ms = System.currentTimeMillis() - start;
        stats.addPipResponse(getName(), ms);
        LOG.info("{} PIP response for {} took {} ms", getName(), userUrn, ms);

        if (CollectionUtils.isEmpty(result)) {
            return empty;
        }
        Identifier dataTypeId = providedAttribute.getDataTypeId();
        List<AttributeValue<?>> stdAttributeValues = result.stream().map(valueIn -> new StdAttributeValue<>(dataTypeId, valueIn)).collect(toList());
        Attribute responseAttr = new StdAttribute(providedAttribute.getCategory(), providedAttribute.getAttributeId(), stdAttributeValues, null, true);
        LOG.debug("Returning result from PIP {}: {}", getName(), result);
        return new StdSinglePIPResponse(responseAttr);
    }

    private Optional<String> getAttribute(PIPRequest pipRequest, PIPFinder pipFinder) throws PIPException {
        PIPResponse matchingAttributes = pipFinder.getMatchingAttributes(pipRequest, this);
        Optional<Attribute> nameAttributeOptional = matchingAttributes.getAttributes().stream().findFirst();
        if (nameAttributeOptional.isEmpty() || CollectionUtils.isEmpty(nameAttributeOptional.get().getValues())) {
            return Optional.empty();
        }
        return Optional.of( (String) nameAttributeOptional.get().getValues().stream().findFirst().get().getValue());
    }

    protected abstract List<Object> getAttributes(String userUrn);

    public abstract String getIdentifierProvidedAttribute();

    public abstract boolean useUnspecifiedURN();
}
