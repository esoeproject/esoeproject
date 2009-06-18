package grails.spep

import grails.test.*

import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.RequestAttributes

import grails.util.GrailsWebUtil

import com.qut.middleware.spep.filter.SPEPFilter

class SpepTests extends GrailsUnitTestCase {

	def testUserAttributes = [:]
	def spep
	def defaultAttribs = [
		uid: ['username'],
		mail: ['test@example.com'],
		roles: ['user', 'admin']
	]
	
	void setUp() {
		GrailsWebUtil.bindMockWebRequest()
		spep = new Spep(enabled: true)
		super.setUp()
	}

	def createCustomUserObject() {
		Class customUserClass = new GroovyClassLoader().parseClass(
			// Alright.. it's not the tidiest idea.. but it works for testing purposes.
			"class TestSpepUserObject{ def uid; def mail; List roles; boolean authenticated }"
		)
		customUserClass.newInstance()
	}

	void setSpepAttributes(attributes) {
		RequestContextHolder.requestAttributes.setAttribute(SPEPFilter.ATTRIBUTES, attributes, RequestAttributes.SCOPE_SESSION)
	}

	void configureDefaultSpep() {
		spep.user = new SpepUser()
		spepAttributes = defaultAttribs
	}
	
    void testAuthenticateDefaultSpepUser() {

		def user = new SpepUser()
		spep.user = user
		spepAttributes = defaultAttribs
		def returnedUser = spep.authenticate()
		
		assertEquals(user, returnedUser)
		assertTrue(user.authenticated)
		
		defaultAttribs.each { k,v->
			assertEquals(v, user.attributes[k])
		}
		
    }

    void testNewAuthenticateCustomSpepUser() {
		spepAttributes = defaultAttribs
		spep.user = createCustomUserObject()
		
		spep.authenticate()
		assertTrue(spep.user.authenticated)
		
		defaultAttribs.each { k,v ->
			if (spep.user[k] instanceof List) {
				assertEquals(v, spep.user[k])
			} else {
				assertEquals(v.first(), spep.user[k])
			}
		}
	}

    void testCallingAuthenticateTwiceDoesntSetAttributesTwice() {
		spep.user = new SpepUser()
		spepAttributes = defaultAttribs
		spep.authenticate()
		
		spep.user.metaClass.setAuthenticated = { fail() }
		
		spep.authenticate()
	}
	
	void testAuthenticatingWhenNotEnabledDoesNothing() {
		spep.enabled = false
		spep.user = new SpepUser()
		spepAttributes = defaultAttribs
		spep.authenticate()
		assertFalse(spep.user.authenticated)
	}
}

