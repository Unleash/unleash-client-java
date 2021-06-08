package io.getunleash.repository;

public interface ToggleBackupHandler {
    ToggleCollection read();

    void write(ToggleCollection toggleCollection);
}
