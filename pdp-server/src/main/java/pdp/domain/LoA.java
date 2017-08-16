package pdp.domain;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

public class LoA {

    private String level;

    @Valid
    private List<PdpAttribute> attributes = new ArrayList<>();

    @Valid
    private List<String> cidrNotations = new ArrayList<>();
}
