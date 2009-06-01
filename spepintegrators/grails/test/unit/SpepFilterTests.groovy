import grails.test.*
import grails.spep.*
import grails.util.*
import org.codehaus.groovy.grails.plugins.web.filters.*
import org.springframework.context.*

class SpepFilterTests extends GrailsUnitTestCase {
	def spepFilterConfig

	def testUserAttributes = [:]
	def session = [:]

	def applicationContext = [:]

	def createCustomUserObject() {
		Class customUserClass = new GroovyClassLoader().parseClass(
			// Alright.. it's not the tidiest idea.. but it works for testing purposes.
			"class TestSpepUserObject{ def uid; def mail; List roles; boolean authenticated }"
		)
		customUserClass.newInstance()
	}

    protected void setUp() {
        super.setUp()

		filterSetUp()
	}

	void filterSetUp() {
		def spepFilters = new SpepFilters()
		spepFilterConfig = new DefaultGrailsFiltersClass(SpepFilters).getConfigs(spepFilters).find{it.name == 'setSpepAttributesInSessionBean'}
		spepFilters.metaClass.applicationContext = null
		spepFilters.metaClass.session = null

		spepFilters.with {
			applicationContext = [getBean:{this.applicationContext[it]}] as ApplicationContext
			session = this.session
		}
	}

    protected void tearDown() {
        super.tearDown()
    }

	void authenticate() {
		testUserAttributes.with {
			uid = ['username']
			mail = ['test@example.com']
			roles = ['user', 'admin']
		}
		session[(com.qut.middleware.spep.filter.SPEPFilter.ATTRIBUTES)] = testUserAttributes
    }

	void setSpepUserBean(bean) {
		applicationContext['spepUser'] = bean
	}

	def getSpepUserBean() {
		applicationContext['spepUser']
	}

    void testNewAuthenticatedSessionDefaultBean() {
		spepUserBean = new SpepUser()
		authenticate()

		spepFilterConfig.before()

		assertTrue(spepUserBean.authenticated)
		testUserAttributes.each { k,v->
			assertEquals(v, spepUserBean."${k}")
			assertEquals(v, spepUserBean.attributes[k])
		}
    }

    void testNewUnauthenticatedSessionDefaultBean() {
		spepUserBean = new SpepUser()

		spepFilterConfig.before()

		assertFalse(spepUserBean.authenticated)
    }

    void testNewAuthenticatedSessionCustomBean() {
		spepUserBean = createCustomUserObject()
		authenticate()

		spepFilterConfig.before()

		assertTrue(spepUserBean.authenticated)
		testUserAttributes.each { k,v->
			if (spepUserBean."${k}" instanceof List) {
				assertEquals(v, spepUserBean."${k}")
			} else {
				assertEquals(v[0], spepUserBean."${k}")
			}
		}
    }

    void testNewUnauthenticatedSessionCustomBean() {
		spepUserBean = createCustomUserObject()

		spepFilterConfig.before()

		assertFalse(spepUserBean.authenticated)
    }

    void testNewRepeatedSessionDefaultBean() {
		spepUserBean = new SpepUser()
		spepUserBean.metaClass.getAuthenticated = {-> true }
		spepUserBean.metaClass.setAuthenticated = { fail() }
		authenticate()

		spepFilterConfig.before()

		testUserAttributes.each { k,v->
			assertEquals(null, spepUserBean."${k}")
			assertEquals(null, spepUserBean.attributes[k])
		}
    }
}

