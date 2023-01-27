package fr.toulouse.iadata.resttemplate.config

import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration

@Configuration
//@EnableAutoConfiguration
@ComponentScan( basePackages = ["fr.toulouse"])
@EnableConfigurationProperties
class TestConfig {
}