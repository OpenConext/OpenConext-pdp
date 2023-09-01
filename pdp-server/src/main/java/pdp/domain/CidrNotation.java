package pdp.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pdp.ip.IPInfo;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CidrNotation {

    private String ipAddress;
    private int prefix;
    private IPInfo ipInfo;

}
