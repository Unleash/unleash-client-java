package io.getunleash.repository;

public interface FeatureBackupHandler {
    FeatureCollection read();

    void write(FeatureCollection featureCollection);
}
