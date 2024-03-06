package pdp.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.util.CollectionUtils;
import pdp.xacml.PolicyTemplateEngine;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY;
import static java.util.stream.Collectors.groupingBy;


@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@ToString
public class PdpPolicyDefinition {

    private Long id;

    @NotNull
    @Size(min = 1)
    private String name;

    @NotNull
    @Size(min = 1)
    private String description;

    @NotNull
    @Size(min = 1)
    private List<String> serviceProviderIds= new ArrayList<>();
    private List<String> serviceProviderNames= new ArrayList<>();
    private List<String> serviceProviderNamesNl= new ArrayList<>();

    private boolean serviceProviderInvalidOrMissing;

    private List<String> identityProviderIds = new ArrayList<>();
    private List<String> identityProviderNames = new ArrayList<>();
    private List<String> identityProviderNamesNl = new ArrayList<>();

    private String clientId;

    @Valid
    private List<PdpAttribute> attributes = new ArrayList<>();

    @Valid
    private List<LoA> loas = new ArrayList<>();

    private String denyAdvice;

    private boolean denyRule;

    private boolean allAttributesMustMatch;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Date created;

    private String userDisplayName;

    private String authenticatingAuthorityName;

    private int numberOfViolations;

    private int numberOfRevisions;

    private String denyAdviceNl;

    private int revisionNbr;

    private boolean isActivatedSr;

    private boolean active;

    private boolean actionsAllowed;

    private String type;

    private Long parentId;

    //used in the mustache templates
    @JsonIgnore
    public List<String> anyIdentityProviders() {
        return CollectionUtils.isEmpty(this.identityProviderIds) ? Collections.emptyList() : Arrays.asList("will-iterate-once");
    }

    @JsonIgnore
    public Set<Map.Entry<String, List<PdpAttribute>>> allAttributesGrouped() {
        return this.attributes.stream().collect(groupingBy(PdpAttribute::getName)).entrySet();
    }

    @JsonIgnore
    public boolean isIdpOnly() {
        return this.identityProviderIds != null && !this.identityProviderIds.isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PdpPolicyDefinition that = (PdpPolicyDefinition) o;
        return Objects.equals(denyRule, that.denyRule) &&
                Objects.equals(allAttributesMustMatch, that.allAttributesMustMatch) &&
                Objects.equals(name, that.name) &&
                Objects.equals(description, that.description) &&
                Objects.equals(serviceProviderIds, that.serviceProviderIds) &&
                Objects.equals(identityProviderIds, that.identityProviderIds) &&
                Objects.equals(attributes, that.attributes) &&
                Objects.equals(denyAdvice, that.denyAdvice) &&
                Objects.equals(denyAdviceNl, that.denyAdviceNl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, description, serviceProviderIds, identityProviderIds, attributes, denyAdvice, denyRule, allAttributesMustMatch);
    }

    public static PdpPolicyDefinition policyDefinition(List<String> serviceProviderIds, List<String> identityProvidersIds) {
        PdpPolicyDefinition definition = new PdpPolicyDefinition();
        definition.setServiceProviderIds(serviceProviderIds);
        definition.setServiceProviderNames(serviceProviderIds);
        definition.setIdentityProviderIds(identityProvidersIds);
        definition.setType("reg");
        return definition;
    }

    @JsonIgnore
    public void sortLoas() {
        Collections.sort(this.loas, Comparator.comparing(LoA::getLevel).reversed());
    }

}
