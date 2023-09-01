package pdp.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.util.CollectionUtils;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.groupingBy;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class LoA {

    private String level;

    private boolean allAttributesMustMatch;

    private boolean negateCidrNotation;

    @Valid
    private List<PdpAttribute> attributes = new ArrayList<>();

    @Valid
    private List<CidrNotation> cidrNotations = new ArrayList<>();

    @JsonIgnore
    public List<String> anyCidrNotations() {
        return CollectionUtils.isEmpty(this.cidrNotations) ? Collections.emptyList() : Arrays.asList("will-iterate-once");
    }

    @JsonIgnore
    public List<String> anyAttributes() {
        return CollectionUtils.isEmpty(this.attributes) ? Collections.emptyList() : Arrays.asList("will-iterate-once");
    }

    @JsonIgnore
    public boolean empty() {
        return CollectionUtils.isEmpty(this.cidrNotations) && CollectionUtils.isEmpty(this.attributes);
    }

    @JsonIgnore
    public Set<Map.Entry<String, List<PdpAttribute>>> allAttributesGrouped() {
        return this.attributes.stream().collect(groupingBy(PdpAttribute::getName)).entrySet();
    }

}
