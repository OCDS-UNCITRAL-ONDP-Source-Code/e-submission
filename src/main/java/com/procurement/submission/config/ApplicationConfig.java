package com.procurement.submission.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
    WebConfig.class,
    ServiceConfig.class,
    DatabaseConfig.class
})
public class ApplicationConfig {
}
