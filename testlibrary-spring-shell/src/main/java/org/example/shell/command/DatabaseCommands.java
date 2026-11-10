package org.example.shell.command;

import org.example.core.auth.annotation.RequirePriority;
import org.example.core.persistence.DatabaseBootstrapper;
import org.springframework.shell.command.annotation.Command;
import org.springframework.shell.command.annotation.Option;
import org.springframework.stereotype.Component;

@Command
@Component
public class DatabaseCommands {

    private final DatabaseBootstrapper databaseBootstrapper;

    public DatabaseCommands(DatabaseBootstrapper databaseBootstrapper) {
        this.databaseBootstrapper = databaseBootstrapper;
    }

    @Command(command = "createDuckDb", description = "Create an empty DuckDB file and execute DDL")
    @RequirePriority({"admin"})
    public String createDuckDb(@Option(longNames = "path", required = true) String path) {
        String jdbcUrl = path.startsWith("jdbc:duckdb:") ? path : "jdbc:duckdb:" + path;
        databaseBootstrapper.initializeEmpty(jdbcUrl);
        return "duckdb created: " + path;
    }
}
