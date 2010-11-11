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
 * @author Ezequiel Martin Apfel
 * @author <a href='mailto:burt@burtbeckwith.com'>Burt Beckwith</a>
 */

import groovy.xml.StreamingMarkupBuilder
import groovy.xml.XmlUtil

includeTargets << new File("$flexPluginDir/scripts/_FlexCommon.groovy")

String grailsAppName = metadata['app.name']
servicePath = "\$DOCUMENTS/$grailsAppName/web-app/WEB-INF/flex/services-config.xml"
serverRoot = "http://localhost:8080/"
serverRootUrl = "http://localhost:8080/$grailsAppName"
localePath = "\$DOCUMENTS/$grailsAppName/grails-app/i18n"

target(integrateWithFlashBuilder: 'Configures project files for Flash Builder') {
	depends(addFlexNature, createDotFlexProperties, createActionScriptProperties)
}

target(addFlexNature: 'Add Flex Nature to .project') {

	File projectFile = new File("$basedir/.project")
	if (!projectFile.exists()) {
		ant.echo ""
		ant.echo "Error: .project not found. Run 'grails integrate-with --eclipse' first and then re-run this script"
		ant.echo ""
		exit 1
	}

	String xml = projectFile.text

	boolean hasBuilder = false
	boolean hasFlexNature = false
	boolean hasAsNature = false

	def root = new XmlSlurper().parseText(xml)

	root.buildSpec.buildCommand.each {
		if (it.name.text() == 'com.adobe.flexbuilder.project.flexbuilder') {
			hasBuilder = true
		}
	}

	root.natures.nature.each {
		if (it.text() == 'com.adobe.flexbuilder.project.flexnature') {
			hasFlexNature = true
		}
		if (it.text() == 'com.adobe.flexbuilder.project.actionscriptnature') {
			hasAsNature = true
		}
	}

	if (hasBuilder && hasFlexNature && hasAsNature) {
		ant.echo '.project file already has Flash Builder support'
		return
	}

	if (!hasBuilder) {
		root.buildSpec.appendNode {
			buildCommand {
				name 'com.adobe.flexbuilder.project.flexbuilder'
				arguments()
			}
		}
	}

	if (!hasFlexNature) {
		root.natures.appendNode {
			nature 'com.adobe.flexbuilder.project.flexnature'
		}
	}

	if (!hasAsNature) {
		root.natures.appendNode {
			nature 'com.adobe.flexbuilder.project.actionscriptnature'
		}
	}

	String newXml = XmlUtil.serialize(new StreamingMarkupBuilder().bind { mkp.yield root })

	String backupName = "$basedir/.project_backup_${System.currentTimeMillis()}"
	ant.copy file: projectFile,
	         tofile: new File(backupName),
	         preservelastmodified: true
	ant.echo "Backed up .project to $backupName"
	projectFile.withWriter { it.write newXml }
	ant.echo "Updated .project for Flash Builder"
}

target(createDotFlexProperties: 'Create .flexProperties') {

	ant.copy file: "$pluginDirPath/src/resources/flashbuilder/eclipse.flexProperties",
	         tofile: "$basedir/.flexProperties", verbose: true

	ant.replace file: "$basedir/.flexProperties",
	            token: "@server-root@", value: serverRoot

	ant.replace file: "$basedir/.flexProperties",
	            token: "@server-root-url@", value: serverRootUrl

	ant.replace file: "$basedir/.flexProperties",
	            token: "@project-name@", value: grailsAppName

	ant.replace file: "$basedir/.flexProperties",
	            token: "@basedir@", value: basedir.replaceAll('\\\\', '/')
}

target(createActionScriptProperties: 'Create .actionScriptProperties') {
	ant.copy file: "$pluginDirPath/src/resources/flashbuilder/eclipse.actionScriptProperties",
	         tofile: "$basedir/.actionScriptProperties", verbose: true

	ant.replace file: "$basedir/.actionScriptProperties",
	            token: "@service-path@", value: servicePath

	ant.replace file: "$basedir/.actionScriptProperties",
	            token: "@output-folder@", value: 'web-app'

	ant.replace file: "$basedir/.actionScriptProperties",
	            token: "@server-root-url@", value: serverRootUrl

	ant.replace file: "$basedir/.actionScriptProperties",
	            token: "@locale-path@", value: localePath

	ant.replace file: "$basedir/.actionScriptProperties",
	            token: "@project-uuid@", value: UUID.randomUUID()

	ant.replace file: "$basedir/.actionScriptProperties",
	            token: "@flex-src@", value: 'web-app'

	ant.replace file: "$basedir/.actionScriptProperties",
	            token: "@lib-dir@", value: 'WEB-INF/flex/user_classes'
}

setDefaultTarget 'integrateWithFlashBuilder'
