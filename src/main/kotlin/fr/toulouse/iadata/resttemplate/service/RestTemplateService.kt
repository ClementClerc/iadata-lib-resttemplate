package fr.toulouse.iadata.resttemplate.service

import mu.KLogger
import mu.KotlinLogging

import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.context.support.GenericWebApplicationContext

@Service
class RestTemplateService(
    private val context : GenericWebApplicationContext,
)
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

    fun getRestTemplateDefault( ) : RestTemplate
    {
        return defaultRestTemplate
    }
}