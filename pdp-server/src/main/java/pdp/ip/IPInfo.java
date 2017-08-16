package pdp.ip;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class IPInfo {

    private String  networkAddress;
    private String broadcastAddress;
    private double capacity;
    private boolean ipv4;

}
