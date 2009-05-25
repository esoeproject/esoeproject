import grails.util.Environment

class SpepFilters {
	def spepUser

	def filters = {
		setSpepAttributesInSessionBean(controller:'*', action:'*') {
			before = {
				def attributes
				switch (Environment.current) {
					case Environment.DEVELOPMENT:
					attributes = grailsApplication.config.spep.devAttributes
					break;

					default:
					attributes = session[com.qut.middleware.spep.filter.SPEPFilter.ATTRIBUTES]
					break;
				}

				// If the user is authenticated..
				if (attributes != null) {
					// .. and the bean does not already reflect this..
					if (!spepUser.authenticated) {
						// .. update it.
						spepUser.attributes = attributes
						spepUser.authenticated = true
					}
				}
				else {
					// To get here, lazy session init would have to be enabled.
					// Pass the unauthenticated status straight through.
					spepUser.attributes = null
					spepUser.authenticated = false
				}
			}
		}
	}
}

