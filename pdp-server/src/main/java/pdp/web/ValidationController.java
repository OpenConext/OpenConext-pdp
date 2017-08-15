package pdp.web;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import pdp.domain.Validation;
import pdp.validations.IPAddressValidator;
import pdp.validations.Validator;

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
}
