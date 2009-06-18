package grails.spep

import grails.web.container.EmbeddableServerFactory
import grails.web.container.EmbeddableServer

public class SpepEnabledJettyServerFactory implements EmbeddableServerFactory {

	public EmbeddableServer createInline(String basedir, String webXml, String contextPath, ClassLoader classLoader) {
		new SpepEnabledJettyServer(basedir, webXml, contextPath, classLoader)
	}

	public EmbeddableServer createForWAR(String warPath, String contextPath) {
		new SpepEnabledJettyServer(warPath, contextPath)
	}
}