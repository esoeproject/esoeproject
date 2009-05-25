import grails.spep.SpepUser
import grails.util.Environment
import org.springframework.aop.scope.ScopedProxyFactoryBean

class SpepGrailsPlugin {
	def version = 0.2
	def dependsOn = [:]

	def author = "Shaun Mangelsdorf"
	def authorEmail = "s.mangelsdorf@gmail.com"
	def title = "Integrates the SPEP filter with a Grails application"
	def description = '''\
Integrates the SPEP filter with a Grails application.
'''

	def documentation = "http://esoeproject.org/wiki/esoe/SPEP_Grails_Plugin"

	def doWithSpring = {
		spepUserSessionBean(SpepUser) {
			it.scope = "session"
			it.autowire = "byName"
		}

		spepUser(ScopedProxyFactoryBean) {
			targetBeanName = "spepUserSessionBean"
			proxyTargetClass = false
		}
	}
   
	def doWithWebDescriptor = { xml ->
		if (Environment.current != Environment.DEVELOPMENT) {
			xml.'filter' + {
				'filter' {
					'filter-class'("com.qut.middleware.spep.filter.SPEPFilter")
					'filter-name'("spep-grails-plugin-filter")
					'init-param' {
						'param-name'("spep-context")
						'param-value'("/spep")
					}
				}
			}
			xml.'filter-mapping' + {
				'filter-mapping' {
					'filter-name'("spep-grails-plugin-filter")
					'url-pattern'("/*")
				}
			}
		}
	}
}
