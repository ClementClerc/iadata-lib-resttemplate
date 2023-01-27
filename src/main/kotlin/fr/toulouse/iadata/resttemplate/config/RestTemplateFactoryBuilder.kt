package fr.toulouse.iadata.resttemplate.config

import fr.toulouse.iadata.resttemplate.properties.RestTemplateProperties
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.context.support.GenericWebApplicationContext
import javax.annotation.PostConstruct

@Service
@Scope("prototype")
class RestTemplateFactoryBuilder
{
    @Autowired private var context : GenericWebApplicationContext? = null
    @Autowired private val restTemplateProperties: RestTemplateProperties? = null
    @Autowired private var restTemplateFactory: RestTemplateFactory? = null

    @PostConstruct
    fun registerBeans(){
        for(restTemplateConfig in restTemplateProperties!!.restTemplateConfigs)
        {
            context!!.registerBean(restTemplateConfig.restTemplateName, RestTemplate::class.java, restTemplateFactory!!.createRestTemplate(restTemplateConfig) )
        }
    }

}