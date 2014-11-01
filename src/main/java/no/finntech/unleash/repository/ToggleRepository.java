package no.finntech.unleash.repository;

import no.finntech.unleash.Toggle;

public interface ToggleRepository {
    Toggle getToggle(String name);
}
