package pdp.ip;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class IPInfo {

    private String networkAddress;
    private String broadcastAddress;
    private double capacity;
    private boolean ipv4;
    private int prefix;

}
