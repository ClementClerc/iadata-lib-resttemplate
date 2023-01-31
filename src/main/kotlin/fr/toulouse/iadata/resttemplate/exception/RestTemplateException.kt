package fr.toulouse.iadata.resttemplate.exception

class WrongConfigurationException(message: String? = null, cause: Throwable? = null) : Exception(message, cause)
class NoRestTemplateFoundException(message: String? = null, cause: Throwable? = null) : Exception(message, cause)