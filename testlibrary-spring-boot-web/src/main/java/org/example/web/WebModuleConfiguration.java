package org.example.web;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan({"org.example.web.controller", "org.example.web.session"})
public class WebModuleConfiguration {
}
