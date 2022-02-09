package io.getunleash.strategy.constraints;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class SemanticVersionTest {

  @Test
  public void obeysSgnRequirementForComparator() throws SemanticVersion.InvalidVersionException {
    SemanticVersion a = SemanticVersion.parse("1.0.0");
    SemanticVersion b = SemanticVersion.parse("1.0.1");
    assertThat(a).isLessThan(b);
    assertThat(b).isGreaterThan(a);
  }

  @Test
  public void isTransitive() throws SemanticVersion.InvalidVersionException {
    SemanticVersion a = SemanticVersion.parse("1.0.0");
    SemanticVersion b = SemanticVersion.parse("1.0.1");
    SemanticVersion c = SemanticVersion.parse("1.1.0");
    assertThat(a).isLessThan(b);
    assertThat(b).isLessThan(c);
    assertThat(a).isLessThan(c);
  }

  @Test
  public void rcIsGreaterThanAlphaAndBetaButSmallerThanRelease()
      throws SemanticVersion.InvalidVersionException {
    SemanticVersion a = SemanticVersion.parse("1.0.0-rc");
    SemanticVersion alpha = SemanticVersion.parse("1.0.0-alpha.1");
    SemanticVersion beta = SemanticVersion.parse("1.0.0-beta.1");
    SemanticVersion release = SemanticVersion.parse("1.0.0");

    assertThat(a).isGreaterThan(alpha);
    assertThat(a).isGreaterThan(beta);
    assertThat(a).isLessThan(release);
  }

  @Test
    public void alphaVersionsAlsoCounted() throws SemanticVersion.InvalidVersionException {
      SemanticVersion alpha = SemanticVersion.parse("1.0.0-alpha.1");
      SemanticVersion alpha2 = SemanticVersion.parse("1.0.0-alpha.2");
      assertThat(alpha.compareTo(alpha2)).isLessThan(0);
      assertThat(alpha).isLessThan(alpha2);
  }
}
