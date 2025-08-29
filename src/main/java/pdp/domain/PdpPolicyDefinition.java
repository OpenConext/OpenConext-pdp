package pdp.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.util.CollectionUtils;

import java.util.*;

import static java.util.stream.Collectors.groupingBy;


@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@ToString
public class PdpPolicyDefinition {

    private String id;

    private String policyId;

    @NotNull
    @Size(min = 1)
    private String name;

    @NotNull
    @Size(min = 1)
    private String description;

    private List<String> serviceProviderIds = new ArrayList<>();
    private List<String> serviceProviderNames = new ArrayList<>();
    private List<String> serviceProviderNamesNl = new ArrayList<>();

    private boolean serviceProvidersNegated;

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
        return CollectionUtils.isEmpty(this.identityProviderIds) ? Collections.emptyList() : List.of("will-iterate-once");
    }

    //used in the mustache templates
    @JsonIgnore
    public List<String> anyServiceProviders() {
        return CollectionUtils.isEmpty(this.serviceProviderIds) ? Collections.emptyList() : List.of("will-iterate-once");
    }

    @JsonIgnore
    public Set<Map.Entry<Map.Entry<String, Integer>, List<PdpAttribute>>> allAttributesGrouped() {
        return this.attributes.stream().collect(groupingBy(attribute -> Map.entry(attribute.getName(), attribute.getGroupID())))
            .entrySet();
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

    @JsonIgnore
    public void sortAttributes() {
        Collections.sort(this.attributes, Comparator.comparing(PdpAttribute::getName));
    }

}
