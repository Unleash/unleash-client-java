package no.finn.unleash.lang;

import javax.annotation.Nonnull;
import javax.annotation.meta.TypeQualifierNickname;
import javax.annotation.meta.When;
import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Documented
@Nonnull(when = When.MAYBE)
@TypeQualifierNickname
@Target({ElementType.METHOD, ElementType.PARAMETER, ElementType.FIELD})
public @interface Nullable {
}
