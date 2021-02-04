package no.finn.unleash.lang;

import javax.annotation.Nonnull;
import javax.annotation.meta.TypeQualifierDefault;
import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Documented
@Nonnull
@TypeQualifierDefault({ElementType.PARAMETER, ElementType.METHOD})
@Target({ElementType.PACKAGE, ElementType.TYPE})
public @interface NonNullApi {
}

