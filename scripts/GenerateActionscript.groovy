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

import groovy.text.SimpleTemplateEngine

includeTargets << new File("$flexPluginDir/scripts/_FlexCommon.groovy")

USAGE = """
Usage: grails generate-actionscript <actionscript class name and package> [destination folder]

Creates an ActionScript class using the specified name. Creates relative to the web-app
folder if no destination is specified, and relative to the destination if it is specified.

Example: grails generate-actionscript com.yourcompany.yourapp.Person
Example: grails generate-actionscript com.yourcompany.yourapp.Organization web-app/flex
"""

target(generateActionscript: 'Creates an ActionScript class') {
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

	templateAttributes = [packageName: packageName,
	                      className: className]

	templateEngine = new SimpleTemplateEngine()

	String dir = packageToDir(packageName)
	generateFile "$pluginDirPath/src/resources/mxml/as.template",
	             "$destDir/${dir}${className}.as"
}

packageToDir = { String packageName ->
	String dir = ''
	if (packageName) {
		dir = packageName.replaceAll('\\.', '/') + '/'
	}
	dir
}

splitClassName = { String fullName ->

	int index = fullName.lastIndexOf('.')
	String packageName = ''
	String className = ''
	if (index > -1) {
		packageName = fullName[0..index-1]
		className = fullName[index+1..-1]
	}
	else { 
		packageName = ''
		className = fullName
	}

	[packageName, className]
}

generateFile = { String templatePath, String outputPath ->
	if (!okToWrite(outputPath)) {
		return
	}

	File templateFile = new File(templatePath)
	if (!templateFile.exists()) {
		ant.echo message: "\nERROR: $templatePath doesn't exist"
		return
	}

	File outFile = new File(outputPath)

	// in case it's in a package, create dirs
	ant.mkdir dir: outFile.parentFile

	outFile.withWriter { writer ->
		templateEngine.createTemplate(templateFile.text).make(templateAttributes).writeTo(writer)
	}

	ant.echo message: "generated $outFile.absolutePath"
}

setDefaultTarget 'generateActionscript'
