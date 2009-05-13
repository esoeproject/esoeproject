//
// This script is executed by Grails after plugin was installed to project.
// This script is a Gant script so you can use all special variables provided
// by Gant (such as 'baseDir' which points on project base dir). You can
// use 'Ant' to access a global instance of AntBuilder
//
// For example you can create directory under project tree:
// Ant.mkdir(dir:"/home/shaun/src/spep/grails-app/jobs")
//

Ant.property(environment:"env")
grailsHome = Ant.antProject.properties."env.GRAILS_HOME"

def contextXml = new File("web-app/META-INF/context.xml")
if (!contextXml.exists()) {
	println "No context.xml .. creating one for you. This will not be automatically removed if you uninstall this plugin."
	Ant.mkdir(dir:contextXml.parent);

	contextXml.write("""<?xml version="1.0" encoding="UTF-8"?>
<Context crossContext="true">
  <WatchedResource>WEB-INF/web.xml</WatchedResource>
  <WatchedResource>META-INF/context.xml</WatchedResource>
</Context>
""");

} else {
	println "${contextXml} already exists. Please ensure that crossContext=\"true\" is specified if not enabled globally."
}

