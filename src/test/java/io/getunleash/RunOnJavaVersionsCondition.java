package io.getunleash;

import static org.junit.platform.commons.util.AnnotationUtils.findAnnotation;

import java.util.Arrays;
import java.util.Optional;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

public class RunOnJavaVersionsCondition implements ExecutionCondition {
    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext extensionContext) {
        Optional<RunOnJavaVersions> annotation =
                findAnnotation(extensionContext.getElement(), RunOnJavaVersions.class);
        return annotation
                .map(
                        a -> {
                            String runtimeVersion = getJavaVersion();
                            if (runtimeVersion != null) {
                                if (Arrays.stream(a.javaVersions())
                                        .anyMatch(runtimeVersion::startsWith)) {
                                    return ConditionEvaluationResult.enabled(
                                            "Runtime java version is included");
                                } else {
                                    return ConditionEvaluationResult.disabled(
                                            "Java version is not among the versions we run this test for");
                                }
                            } else {
                                return ConditionEvaluationResult.enabled(
                                        "Couldn't find a java version to compare for");
                            }
                        })
                .orElse(ConditionEvaluationResult.enabled("No annotation found"));
    }

    private String getJavaVersion() {
        return System.getProperty("java.version");
    }
}
