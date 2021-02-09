package no.finn.unleash.lang;

import java.lang.annotation.*;
import javax.annotation.Nonnull;
import javax.annotation.meta.TypeQualifierDefault;

@Retention(RetentionPolicy.RUNTIME)
@Documented
@Nonnull
@TypeQualifierDefault({ElementType.PARAMETER, ElementType.METHOD})
@Target({ElementType.PACKAGE, ElementType.TYPE})
public @interface NonNullApi {}
