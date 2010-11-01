/**
 * Copyright 2009-2010 the original author or authors.
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

/**
 * @author <a href='mailto:burt@burtbeckwith.com'>Burt Beckwith</a>
 */

import grails.util.Environment

includeTargets << grailsScript('_GrailsBootstrap')

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

antProperty = { String key -> ant.project.properties[key] }

pluginDirPath = flexPluginDir.path

overwriteAll = false

okToWrite = { String dest ->

	def file = new File(dest)
	if (overwriteAll || !file.exists()) {
		return true
	}

	String propertyName = "file.overwrite.$file.name"
	ant.input addProperty: propertyName, message: "$dest exists, ok to overwrite?",
	          validargs: 'y,n,a', defaultvalue: 'y'

	if (ant.antProject.properties."$propertyName" == 'n') {
		return false
	}

	if (ant.antProject.properties."$propertyName" == 'a') {
		overwriteAll = true
	}

	true
}

copyFile = { String from, String to, boolean verbose = false ->
	if (!okToWrite(to)) {
		return
	}

	ant.copy file: from, tofile: to, overwrite: true, verbose: verbose
}
