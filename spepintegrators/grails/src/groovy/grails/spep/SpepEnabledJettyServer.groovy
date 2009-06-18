package grails.spep

import org.codehaus.groovy.grails.web.container.JettyServer
import org.mortbay.jetty.webapp.WebAppClassLoader
import org.mortbay.jetty.webapp.WebAppContext
import org.mortbay.jetty.Server

public class SpepEnabledJettyServer extends JettyServer {

	WebAppContext spepContext

	public SpepEnabledJettyServer(String warPath, String contextPath) {
		super(warPath, contextPath)
	}

	public SpepEnabledJettyServer(String basedir, String webXml, String contextPath, ClassLoader classLoader) {
		super(basedir, webXml, contextPath, classLoader)
	}

	protected initialize() {
		super.initialize()
		def war = new File(System.getProperty("grails.spep.war"))
		
		spepContext = new WebAppContext(war: war.path, contextPath:"/spep")
		spepContext.parentLoaderPriority = false
		spepContext.systemClasses = [] as String[]

		
		def cl = new WebAppClassLoader(spepContext)
		def deps = new File(System.getProperty("grails.spep.war.dependencies"))
		deps.eachFile { file ->
			if (file.name.endsWith("jar"))
				cl.addURL(file.toURL())
		}

		spepContext.classLoader = cl
	}

	protected Server configureHttpServer(WebAppContext context, int serverPort = DEFAULT_PORT, String serverHost = DEFAULT_HOST) {
		def server = super.configureHttpServer(context, serverPort, serverHost)
		server.addHandler(spepContext)
		server
	} 
}