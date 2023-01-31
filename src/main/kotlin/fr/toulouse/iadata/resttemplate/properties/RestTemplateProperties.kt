package fr.toulouse.iadata.resttemplate.properties

import fr.toulouse.iadata.resttemplate.config.RestTemplateConfig
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "templates")
data class RestTemplateProperties(
    var restTemplateConfigs : List<RestTemplateConfig> = ArrayList()
    ){
}