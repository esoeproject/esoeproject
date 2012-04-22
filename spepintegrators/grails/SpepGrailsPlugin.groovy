import grails.spep.Spep
import grails.spep.SpepUser
import grails.util.Environment
import org.springframework.aop.scope.ScopedProxyFactoryBean
import org.codehaus.groovy.grails.web.servlet.WrappedResponseHolder

class SpepGrailsPlugin {
	def version = "1.2"
    def grailsVersion = "1.1"   // not sure if this is correct, but the plugin doesn't work with at least 1.2 because of the jetty dependencies
	def dependsOn = [:]

	def author = "Shaun Mangelsdorf"
	def authorEmail = "s.mangelsdorf@gmail.com"
	def title = "Integrates the SPEP filter with a Grails application"
	def description = '''\
Integrates the SPEP filter with a Grails application.
'''

	def documentation = "http://esoeproject.qut.edu.au/wiki/esoe/SPEP_Grails_Plugin"

	final static DEFAULT_CONFIG = [
		beanName: "spepUser", 
		userClass: SpepUser, 
		enabled: false, 
		logoutUrl: null
	]
	
	void rationaliseConfig(config) {
		if (config.containsKey('spep')) {
			DEFAULT_CONFIG.each {
				if (!config.containsKey(it.key)) {
					config[it.key] = it.value
				}
			}
		} else {
			config.spep = DEFAULT_CONFIG.clone()
		}
	}
	
	def doWithSpring = {
		rationaliseConfig(application.config)

		if (application.config.spep.enabled && application.config.spep.userClass) {
			
			spepUserSessionBean(application.config.spep.userClass) {
				it.scope = "session"
				it.autowire = "byName"
			}

			"${application.config.spep.beanName}"(ScopedProxyFactoryBean) {
				targetBeanName = "spepUserSessionBean"
				proxyTargetClass = false
			}
		} else {
			log.info("SPEP: enabled is false")
		}
		
		spep(Spep) {
			enabled = application.config.spep.enabled
			user = ref(application.config.spep.beanName)
		}
		
	}
   
	def doWithWebDescriptor = { xml ->
		if (application.config.spep.enabled) {
            // the order of filters in the XML file seems to matter ... and as of tomcat 7, you can't have duplicate definitions (which is what this used to create)
            def filters = xml.'filter'
			filters[0] + {
				'filter' {
					'filter-class'("com.qut.middleware.spep.filter.SPEPFilter")
					'filter-name'("spep-grails-plugin-filter")
					'init-param' {
						'param-name'("spep-context")
						'param-value'(application.config.spep.contextName ?: "/spep")
					}
				}
			}
            def filterMappings = xml.'filter-mapping'
			filterMappings[0] + {
				'filter-mapping' {
					'filter-name'("spep-grails-plugin-filter")
					'url-pattern'("/*")
				}
			}
		}
	}
	
	def doWithDynamicMethods = {
		if (application.config.spep.enabled) {
			application.config.spep.userClass.metaClass.logout = { ->
				def response = WrappedResponseHolder.wrappedResponse
				def logoutUrl = application.config.spep.logoutUrl

				if (response) {
					if (logoutUrl) {
						response.sendRedirect(logoutUrl)
					} else {
						throw new IllegalStateException("Cannot logout spep user as spep.logoutUrl is not defined in application config")
					}
				} else {
					throw new IllegalStateException("Cannot logout spep user as response is unavailable")
				}

				false
			}
		}
	}
}
