/* Copyright 2004-2005 Graeme Rocher
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package grails.spep

import org.codehaus.groovy.grails.web.container.JettyServer
import grails.web.container.EmbeddableServer
import grails.util.BuildSettingsHolder
import grails.util.BuildSettings
import org.mortbay.jetty.webapp.WebAppContext
import org.mortbay.jetty.nio.SelectChannelConnector
import org.mortbay.jetty.Server
import org.mortbay.jetty.Connector
import org.mortbay.jetty.security.SslSocketConnector
import sun.security.tools.KeyTool
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.FileSystemResource
import org.springframework.util.FileCopyUtils
import org.codehaus.groovy.grails.commons.ConfigurationHolder

/**
 * An implementation of the EmbeddableServer interface for Jetty.
 *
 * @see EmbeddableServer
 *
 * @author Graeme Rocher
 * @since 1.1
 * 
 * Created: Jan 7, 2009
 */

public class SpepEnabledJettyServer extends JettyServer {
/*
    BuildSettings buildSettings
    ConfigObject config
    WebAppContext context
    Server grailsServer
    def eventListener

    protected String keystore
    protected File keystoreFile
    protected String keyPassword*/
	WebAppContext spepContext

    /**
     * Creates a new JettyServer for the given war and context path
     */
    public SpepEnabledJettyServer(String warPath, String contextPath) {
        super(warPath, contextPath)
    }

    /**
     * Constructs a Jetty server instance for the given arguments. Used for inline, non-war deployment
     *
     * @basedir The web application root
     * @webXml The web.xml definition
     * @contextPath The context path to deploy to
     * @classLoader The class loader to use
     */
    public SpepEnabledJettyServer(String basedir, String webXml, String contextPath, ClassLoader classLoader) {
        super(basedir, webXml, contextPath, classLoader)
    }

    /**
     * Initializes the JettyServer class
     */
    protected initialize() {
		super.initialize()
		def war = new File(System.getProperty("grails.spep.war"))
		
		spepContext = new WebAppContext(war: war.path, contextPath:"/spep")
		spepContext.parentLoaderPriority = false
		spepContext.systemClasses = [] as String[]

		
		def cl = new org.mortbay.jetty.webapp.WebAppClassLoader(spepContext)
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
        return server
    } 


}