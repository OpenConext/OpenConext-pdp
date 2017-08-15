package pdp.validations;

import java.util.Optional;

public interface Validator {

    String getType();

    Optional<String> validate(String subject);

}
