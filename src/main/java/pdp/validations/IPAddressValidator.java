package pdp.validations;

import org.apache.commons.validator.routines.InetAddressValidator;

import java.util.Optional;

public class IPAddressValidator implements Validator{

    @Override
    public String getType() {
        return "ip";
    }

    @Override
    public Optional<String> validate(String subject) {
        return InetAddressValidator.getInstance().isValid(subject) ? Optional.empty() : Optional.of("Not a valid InetAddress");
    }
}
