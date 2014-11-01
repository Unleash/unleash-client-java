package no.finntech.unleash.repository;

import no.finntech.unleash.UnleashException;

public interface ToggleFetcher {
   Response fetchToggles() throws UnleashException;
}
