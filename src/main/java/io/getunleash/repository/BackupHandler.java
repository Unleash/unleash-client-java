package io.getunleash.repository;

import java.util.Optional;

public interface BackupHandler {
    Optional<String> read();

    void write(String collection);
}
