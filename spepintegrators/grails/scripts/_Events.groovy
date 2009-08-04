eventPackagingEnd = {
	if (config.spep?.enabled) {
		def data = config.spep.data

		if (data) {
			System.setProperty("spep.data", data)
		} else {
			ant.echo("SPEP: 'spep.data' is not specified in the application config for this environment")
		}

		System.setProperty("grails.server.factory", "grails.spep.SpepEnabledJettyServerFactory")

		def baseSpepWarDir = new File("$spepPluginDir/spep-war").canonicalPath

		System.setProperty("grails.spep.war", "$baseSpepWarDir/spep.war")
		System.setProperty("grails.spep.war.dependencies", "$baseSpepWarDir/lib")
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