package fr.toulouse.iadata.resttemplate

import fr.toulouse.iadata.resttemplate.config.RestTemplateFactoryBuilder
import fr.toulouse.iadata.resttemplate.config.TestConfig
import fr.toulouse.iadata.resttemplate.properties.RestTemplateProperties
import org.junit.jupiter.api.Test
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest( classes =  [TestConfig::class] )
class ResttemplateApplicationTests {

    @Test
    fun contextLoads() {
    }

}
