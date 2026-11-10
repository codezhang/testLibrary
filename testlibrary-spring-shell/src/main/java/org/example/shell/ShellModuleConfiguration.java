package org.example.shell;

import org.example.shell.auth.AuthCommands;
import org.example.shell.command.BookingCommands;
import org.example.shell.command.DatabaseCommands;
import org.example.shell.command.UserCommands;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.shell.command.annotation.EnableCommand;

@Configuration
@ComponentScan("org.example.shell")
@EnableCommand({UserCommands.class, BookingCommands.class, AuthCommands.class, DatabaseCommands.class})
public class ShellModuleConfiguration {
}
