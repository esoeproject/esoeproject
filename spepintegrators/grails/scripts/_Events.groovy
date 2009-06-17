eventPackagingEnd = {
	if (config.spep?.enabled) {
		def data = config.spep.data

		if (!data) {
			System.err.println "SPEP: 'spep.data' is not specified in the application config for this environment" 
			System.exit 1
		}

		System.setProperty("spep.data", data)

		System.setProperty("grails.server.factory", "grails.spep.SpepEnabledJettyServerFactory")

		def baseSpepWarDir = new File("$spepPluginDir/spep-war/").canonicalPath

		def vmVersion = System.getProperty("java.vm.version")

		def spepVersion
		if (vmVersion =~ /^1\.5/) {
			spepVersion = "java-1.5"
		} else if (vmVersion =~ /^1\.6/) {
			spepVersion = "java-1.6"
		} else {
			throw new IllegalStateException("SPEP Plugin only supported on JVM 1.5 and 1.6")
		}

		System.setProperty("grails.spep.war", "$baseSpepWarDir/$spepVersion/spep.war")
		System.setProperty("grails.spep.war.dependencies", "$baseSpepWarDir/$spepVersion/spep-endorsed")
	}
}

eventCreateWarStart = { warName, stagingDir ->
	def contextXml = new File("$stagingDir/META-INF/context.xml")
	if (!contextXml.exists()) {
		ant.mkdir(dir:contextXml.parent);

		contextXml.write("""<?xml version="1.0" encoding="UTF-8"?>
<Context crossContext="true">
	<WatchedResource>WEB-INF/web.xml</WatchedResource>
	<WatchedResource>META-INF/context.xml</WatchedResource>
</Context>""");

	} else {
		ant.echo "SPEP: ${contextXml} already exists. Please ensure that crossContext=\"true\" is specified if not enabled globally."
	}
}