package fr.toulouse.iadata.resttemplate.service

import fr.toulouse.iadata.resttemplate.config.RestTemplateConfig
import fr.toulouse.iadata.resttemplate.config.RestTemplateFactory
import fr.toulouse.iadata.resttemplate.config.RestTemplateFactoryBuilder
import fr.toulouse.iadata.resttemplate.exception.BeanAlreadyDefinedInContext
import fr.toulouse.iadata.resttemplate.exception.NoRestTemplateFoundException
import fr.toulouse.iadata.resttemplate.properties.RestTemplateProperties
import mu.KLogger
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired

import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.context.support.GenericWebApplicationContext
import java.util.function.Supplier

@Service
class RestTemplateService(
    private val context : GenericWebApplicationContext)

{

    private val log: KLogger = KotlinLogging.logger {  }
    private val defaultRestTemplate = RestTemplate()
    fun getRestTemplate( beanName : String ) : RestTemplate
    {
        return try {
            context.getBean( beanName ) as RestTemplate
        }
        catch ( e : Exception )
        {
            log.error ("[REST TEMPLATE SERVICE] No rest template found for bean name $beanName", e )
            defaultRestTemplate
        }
    }

    fun reloadRestTemplate(beanName : String, restTemplateProperties: RestTemplateProperties) {

        context.removeBeanDefinition(beanName)
        var restTemplateConfig : RestTemplateConfig? = restTemplateProperties.getRestTemplateConfigByName(beanName)
        if ( restTemplateConfig != null){
            var restTemplate : RestTemplate = RestTemplateFactory().createRestTemplate(restTemplateConfig = restTemplateConfig)
            registerRestTemplate(beanName, restTemplate)
        }else {
            throw NoRestTemplateFoundException(beanName)
        }


    }

    fun registerRestTemplate(beanName : String, restTemplate: RestTemplate ){
        if (!context.containsBean(beanName)) {
            context.removeBeanDefinition(beanName)
            context.registerBean(beanName, RestTemplate::class.java, Supplier { restTemplate })
            log.info ( "[REST-TEMPLATE] registering rest template with name : $beanName" )
        }else {
            throw BeanAlreadyDefinedInContext()
        }
    }

    fun getRestTemplateDefault( ) : RestTemplate
    {
        return defaultRestTemplate
    }
}