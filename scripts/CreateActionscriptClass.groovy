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
Usage: grails create-actionscript-class <actionscript class name and package> [destination folder]

Creates an ActionScript class using the specified name. Creates relative to the web-app
folder if no destination is specified, and relative to the destination if it is specified.

Example: grails create-actionscript-class com.yourcompany.yourapp.Person
Example: grails create-actionscript-class com.yourcompany.yourapp.Organization web-app/flex
"""

target(createActionscriptClass: 'Creates an ActionScript class') {
	depends(checkVersion, configureProxy)

	args = args ? args.split('\n') : []

	String fullClassName
	File destDir
	switch (args.size()) {
		case 1:
			fullClassName = args[0]
			destDir = new File(basedir, 'web-app')
			break
		case 2:
			fullClassName = args[0]
			destDir = new File(new File(args[1]).canonicalPath)
			break
		default:
			ant.echo message: USAGE
			exit 1
			break
	}

	String packageName
	String className
	(packageName, className) = splitClassName(fullClassName)

	templateAttributes.packageName = packageName
	templateAttributes.className = className

	String dir = packageToDir(packageName)
	generateFile "$pluginDirPath/src/resources/mxml/as.template",
	             "$destDir/${dir}${className}.as"
}

setDefaultTarget 'createActionscriptClass'
