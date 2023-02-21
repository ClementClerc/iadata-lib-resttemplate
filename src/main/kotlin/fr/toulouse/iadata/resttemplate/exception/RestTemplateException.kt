package fr.toulouse.iadata.resttemplate.exception

class WrongConfigurationException(message: String? = null, cause: Throwable? = null) : Exception(message, cause)
class NoRestTemplateFoundException(message: String? = null, cause: Throwable? = null) : Exception(message, cause)
class BeanAlreadyDefinedInContext : Exception {
    constructor() : super()
    constructor(beanName: String) : super("[REST-TEMPLATE-SERVICE] No restTemplate Found for name : " + beanName)
    constructor(beanName: String, cause: Throwable) : super("[REST-TEMPLATE-SERVICE] No restTemplate Found for name : " +beanName, cause)
    constructor( cause: Throwable) : super( cause)

}