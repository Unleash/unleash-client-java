package io.getunleash.repository;

public interface BackupHandler<T> {
    T read();

    void write(T collection);
}
