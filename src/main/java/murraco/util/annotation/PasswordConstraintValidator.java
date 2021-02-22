package murraco.util.annotation;

import murraco.exception.NotValidPasswordException;
import org.passay.*;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Arrays;
import java.util.List;

public class PasswordConstraintValidator implements ConstraintValidator<ValidPassword, String> {

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        // Constraint rule set
        PasswordValidator validator = new PasswordValidator(Arrays.asList(
                // needs at least 8 characters and at most 100 chars
                new LengthRule(8, 100),
                // at least one upper-case character
                new CharacterRule(EnglishCharacterData.UpperCase, 1),
                // at least one lower-case character
                new CharacterRule(EnglishCharacterData.LowerCase, 1),
                // at least one digit character
                new CharacterRule(EnglishCharacterData.Digit, 1),
                // at least one symbol (special character)
                new CharacterRule(EnglishCharacterData.Special, 1),
                // no whitespace
                new WhitespaceRule()
        ));
        // validating password with rule set
        RuleResult result = validator.validate(new PasswordData(password));
        if (result.isValid()) {
            return true;
        }
        // if not valid, set messages
        List<String> messages = validator.getMessages(result);
        String messageTemplate = String.join(",", messages);

        throw new NotValidPasswordException(messageTemplate);
    }
}
