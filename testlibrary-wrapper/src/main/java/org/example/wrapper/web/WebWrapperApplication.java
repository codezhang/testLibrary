package org.example.wrapper.web;

import org.example.core.auth.config.AuthCoreConfiguration;
import org.example.core.config.CoreServiceConfiguration;
import org.example.wrapper.common.WrapperCommonConfiguration;
import org.example.web.WebModuleConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.shell.boot.ApplicationRunnerAutoConfiguration;
import org.springframework.shell.boot.ShellRunnerAutoConfiguration;

@SpringBootApplication(exclude = {
        ShellRunnerAutoConfiguration.class,
        ApplicationRunnerAutoConfiguration.class
})
@Import({WrapperCommonConfiguration.class,
        AuthCoreConfiguration.class,
        CoreServiceConfiguration.class,
        WebModuleConfiguration.class})
public class WebWrapperApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebWrapperApplication.class, args);
    }
}
