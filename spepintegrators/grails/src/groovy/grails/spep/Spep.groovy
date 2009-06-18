package grails.spep

import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.RequestAttributes

import com.qut.middleware.spep.filter.SPEPFilter

class Spep {

	boolean enabled
	def user
	
	def authenticate() {
		if (enabled && !user.authenticated) {
			user.metaPropertyValues.each {
				if (it.name == 'attributes') {
					it.value = attributes
				} else if (attributes && attributes[it.name]) {
					this.assign(it, attributes[it.name])
				}
			}
			user.authenticated = true
		}
		user
	}
	
	private assign(property, values) {
		if (List.isAssignableFrom(property.type)) {
			property.value = values
		} else if (String == property.type || Object == property.type) {
			if (values.size() == 0) property.value = null
			else property.value = values[0]
		}
	}
	
	def getAttributes() {
		RequestContextHolder.requestAttributes.getAttribute(SPEPFilter.ATTRIBUTES, RequestAttributes.SCOPE_SESSION)
	}
}