class SpepGrailsPlugin {
    def version = 0.1
    def dependsOn = [:]

    // TODO Fill in these fields
    def author = "Shaun Mangelsdorf"
    def authorEmail = "s.mangelsdorf@gmail.com"
    def title = "Integrates the SPEP filter with a Grails application"
    def description = '''\
Integrates the SPEP filter with a Grails application.
'''

    // URL to the plugin's documentation
    def documentation = "http://esoeproject.org/wiki/esoe/SPEP_Grails_Plugin"

    def doWithSpring = {
        // TODO Implement runtime spring config (optional)
    }
   
    def doWithApplicationContext = { applicationContext ->
        // TODO Implement post initialization spring config (optional)		
    }

    def doWithWebDescriptor = { xml ->
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
	                                      
    def doWithDynamicMethods = { ctx ->
        // TODO Implement registering dynamic methods to classes (optional)
    }
	
    def onChange = { event ->
        // TODO Implement code that is executed when any artefact that this plugin is
        // watching is modified and reloaded. The event contains: event.source,
        // event.application, event.manager, event.ctx, and event.plugin.
    }

    def onConfigChange = { event ->
        // TODO Implement code that is executed when the project configuration changes.
        // The event is the same as for 'onChange'.
    }
}
