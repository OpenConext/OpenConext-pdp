package pdp.web;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pdp.domain.Validation;
import pdp.ip.IPInfo;
import pdp.util.CIDRUtils;
import pdp.validations.IPAddressValidator;
import pdp.validations.Validator;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;

@RestController
public class ValidationController {

    private final Map<String,Validator> validators;

    public ValidationController() {
        this.validators = Arrays.asList(
            new IPAddressValidator()).stream().collect(toMap(Validator::getType, Function.identity()));
    }


    @PostMapping({"/internal/validate", "/protected/validate"})
    public boolean validation(@Validated @RequestBody Validation validation) {
        return !validators.computeIfAbsent(validation.getType(), key -> {
            throw new IllegalArgumentException(String.format("No validation defined for %s", key));
        }).validate(validation.getValue()).isPresent();
    }

    @GetMapping({"/internal/ipinfo", "/protected/ipinfo"})
    public IPInfo ipInfo(@RequestParam String ipAddress,
                         @RequestParam(required = false) Integer networkPrefix) throws UnknownHostException {
        if (!validation(new Validation("ip", ipAddress))) {
            return new IPInfo();
        }
        InetAddress address = InetAddress.getByName(ipAddress);
        boolean isIpv4 = address instanceof Inet4Address;
        if (networkPrefix == null) {
            networkPrefix = isIpv4 ? 24 : 64;
        }
        CIDRUtils cidrUtils = new CIDRUtils(ipAddress.concat("/").concat(networkPrefix.toString()));
        int byteSize = isIpv4 ? 32 : 128;
        double capacity = Math.pow(2, byteSize - networkPrefix);
        return new IPInfo(cidrUtils.getNetworkAddress(), cidrUtils.getBroadcastAddress(),capacity, isIpv4, networkPrefix);
    }
}
