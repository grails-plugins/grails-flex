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

includeTargets << new File("$flexPluginDir/scripts/_FlexCommon.groovy")
includeTargets << new File("$flexPluginDir/scripts/_GenerateTasks.groovy")

USAGE = """
Usage: grails generate-actionscript-class <actionscript class name> <domain class name> [destination folder]

Generates an ActionScript data class from a domain class. Creates relative to the web-app
folder if no destination is specified, and relative to the destination if it is specified.

Example: grails generate-actionscript-class com.yourcompany.yourapp.Person com.yourcompany.yourapp.Person
Example: grails generate-actionscript-class yourapp.Organization com.yourcompany.yourapp.Organization web-app/flex
"""

target(generateActionscriptClass: 'Generates an ActionScript class from a domain class') {
	depends(checkVersion, configureProxy, packageApp, classpath, bootstrap)

	args = args ? args.split('\n') : []

	String asClassName
	String domainClassName
	File destDir
	switch (args.size()) {
		case 2:
			asClassName = args[0]
			domainClassName = args[1]
			destDir = new File(basedir, 'web-app')
			break
		case 3:
			asClassName = args[0]
			domainClassName = args[1]
			destDir = new File(new File(args[2]).canonicalPath)
			break
		default:
			ant.echo message: USAGE
			exit 1
			break
	}

	def domainClass = grailsApp.getDomainClass(domainClassName)
	if (!domainClass) {
		event("StatusFinal", ["No domain class found for name ${domainClassName}. Please try again and enter a valid domain class name"])
		exit 1
	}

	String packageName
	String className
	(packageName, className) = splitClassName(asClassName)

	templateAttributes.packageName = packageName
	templateAttributes.className = className
	templateAttributes.domainClass = domainClass

	String dir = packageToDir(packageName)
	generateFile "$pluginDirPath/src/resources/mxml/DomainClass.as.template",
	             "$destDir/${dir}${className}.as"
}

setDefaultTarget 'generateActionscriptClass'
