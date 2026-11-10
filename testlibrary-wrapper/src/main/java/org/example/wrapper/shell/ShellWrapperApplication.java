package org.example.wrapper.shell;

import org.example.core.auth.config.AuthCoreConfiguration;
import org.example.core.config.CoreServiceConfiguration;
import org.example.shell.ShellModuleConfiguration;
import org.example.wrapper.common.WrapperCommonConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import({WrapperCommonConfiguration.class,
        AuthCoreConfiguration.class,
        CoreServiceConfiguration.class,
        ShellModuleConfiguration.class})
public class ShellWrapperApplication {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(ShellWrapperApplication.class);
        application.setWebApplicationType(WebApplicationType.NONE);
        application.run(args);
    }
}
