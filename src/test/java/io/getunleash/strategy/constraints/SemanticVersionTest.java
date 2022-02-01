package io.getunleash.strategy.constraints;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class SemanticVersionTest {

    @Test
    public void obeysSgnRequirementForComparator() throws SemanticVersion.InvalidVersionException {
        SemanticVersion a = SemanticVersion.parse("1.0.0");
        SemanticVersion b = SemanticVersion.parse("1.0.1");
        assertThat(a.compareTo(b)).isLessThan(0);
        assertThat(b.compareTo(a)).isGreaterThan(0);
    }

    @Test
    public void isTransitive() throws SemanticVersion.InvalidVersionException {
        SemanticVersion a = SemanticVersion.parse("1.0.0");
        SemanticVersion b = SemanticVersion.parse("1.0.1");
        SemanticVersion c = SemanticVersion.parse("1.1.0");
        assertThat(a.compareTo(b)).isLessThan(0);
        assertThat(b.compareTo(c)).isLessThan(0);
        assertThat(a.compareTo(c)).isLessThan(0);

    }
}
