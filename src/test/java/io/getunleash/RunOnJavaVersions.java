package io.getunleash;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.junit.jupiter.api.extension.ExtendWith;

@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(RunOnJavaVersionsCondition.class)
public @interface RunOnJavaVersions {
    String[] javaVersions();
}
