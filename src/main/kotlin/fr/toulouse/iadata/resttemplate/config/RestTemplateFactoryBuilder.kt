package fr.toulouse.iadata.resttemplate.config

import fr.toulouse.iadata.resttemplate.beans.RestTemplateType
import fr.toulouse.iadata.resttemplate.properties.RestTemplateProperties
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.config.BeanDefinition
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.context.support.GenericWebApplicationContext
import java.util.function.Supplier
import javax.annotation.PostConstruct

@Configuration
class RestTemplateFactoryBuilder(
     private var context : GenericWebApplicationContext,
     private val restTemplateProperties: RestTemplateProperties,
     private var restTemplateFactory: RestTemplateFactory
)
{
    @PostConstruct
    fun registerBeans(){
        for(restTemplateConfig in restTemplateProperties.restTemplateConfigs)
        {
            if ( restTemplateConfig.restTemplateType != RestTemplateType.NONE )
            {
                var restTemplate : RestTemplate =  restTemplateFactory!!.createRestTemplate( restTemplateConfig)
                context!!.registerBean( restTemplateConfig.restTemplateName, RestTemplate::class.java, Supplier{restTemplate} )
            }
        }
    }

}