import grails.util.Environment

def configClass = new GroovyClassLoader(getClass().classLoader).parseClass(new File(
	"$basedir/grails-app/conf/Config.groovy"))
def config = new ConfigSlurper(Environment.current.name()).parse(configClass)
flexConfig = config.grails.plugin.flex

ant.property environment: 'env'

flexHome = ant.project.properties.'env.FLEX_HOME'
if (!flexHome) {
	flexHome = flexConfig.home
}

if (flexHome) {
	// FLEX_HOME is needed by mxmlc
	ant.property name: 'FLEX_HOME', value: flexHome
}
