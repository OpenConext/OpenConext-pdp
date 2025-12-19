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

    private boolean serviceProvidersNegated;

    private boolean serviceProviderInvalidOrMissing;

    private List<String> identityProviderIds = new ArrayList<>();

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

    //used in the mustache templates
    @JsonIgnore
    public Set<Map.Entry<Map.Entry<String, Integer>, List<PdpAttribute>>> allAttributesGrouped() {
        return this.attributes.stream().collect(groupingBy(attribute -> Map.entry(attribute.getName(), attribute.getGroupID())))
            .entrySet();
    }

    //used in the mustache templates
    @JsonIgnore
    public boolean isIdpOnly() {
        return this.identityProviderIds != null && !this.identityProviderIds.isEmpty();
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
