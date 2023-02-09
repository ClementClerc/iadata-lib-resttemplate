package fr.toulouse.iadata.resttemplate.config

import fr.toulouse.iadata.resttemplate.properties.RestTemplateProperties
import jakarta.annotation.PostConstruct
import mu.KLogger
import mu.KotlinLogging
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate
import org.springframework.web.context.support.GenericWebApplicationContext
import java.util.function.Supplier

@Configuration
class RestTemplateFactoryBuilder(
    private var context : GenericWebApplicationContext,
    private val restTemplateProperties: RestTemplateProperties,
    private var restTemplateFactory: RestTemplateFactory
)
{
    private var log : KLogger =  KotlinLogging.logger {  }
    @PostConstruct
    fun registerBeans(){
        for(restTemplateConfig in restTemplateProperties.restTemplateConfigs)
        {
            var restTemplate : RestTemplate =  restTemplateFactory.createRestTemplate( restTemplateConfig)
            context.registerBean( restTemplateConfig.restTemplateName, RestTemplate::class.java, Supplier{restTemplate} )
            log.info { "[REST-TEMPLATE] registering rest template with name : $restTemplateConfig." }
        }
    }

}