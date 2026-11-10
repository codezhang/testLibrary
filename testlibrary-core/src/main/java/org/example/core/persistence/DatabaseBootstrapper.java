package org.example.core.persistence;

public interface DatabaseBootstrapper {

    void initializeEmpty(String jdbcUrl);
}
