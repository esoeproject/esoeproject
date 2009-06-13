import com.qut.middleware.spep.filter.SPEPFilter

class SpepFilters {
	
	def grailsApplication
	
	def filters = {
		if (grailsApplication.config.spep.enabled) {
			setSpepAttributesInSessionBean(controller:'*', action:'*') {
				before = {
					def spepUser = applicationContext.getBean(grailsApplication.config.spep.beanName)
					def attributes = session[SPEPFilter.ATTRIBUTES]

					// If the user is authenticated but we haven't updated the spepUser object yet
					if (attributes && !spepUser.authenticated) {
						spepUser.metaPropertyValues.each {
							if (it.name == 'attributes') {
								it.value = attributes
							} else if (attributes && attributes[it.name]) {
								this.assign(it, attributes[it.name])
							}
						}
						spepUser.authenticated = true
					}
				}
			}
		}
	}

	// Convenience method to reduce the level of nesting
	void assign(property, values) {
		if (List.isAssignableFrom(property.type)) {
			property.value = values
		} else if (String == property.type || Object == property.type) {
			if (values.size() == 0) property.value = null
			else property.value = values[0]
		}
	}
}

