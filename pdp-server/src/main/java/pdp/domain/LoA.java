package pdp.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class LoA {

    private String level;

    private boolean allAttributesMustMatch;

    @Valid
    private List<PdpAttribute> attributes = new ArrayList<>();

    @Valid
    private List<CidrNotation> cidrNotations = new ArrayList<>();
}
