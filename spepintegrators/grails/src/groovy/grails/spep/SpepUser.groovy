package grails.spep

import org.codehaus.groovy.runtime.MetaClassHelper

class SpepUser {
	boolean authenticated = false
	def attributes = [:]

	def propertyMissing(String name, value) {
		throw new ReadOnlyPropertyException(name, Object)
	}

	def propertyMissing(String name) {
		this.attributes[name]
	}
}
