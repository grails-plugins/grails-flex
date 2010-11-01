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

USAGE = """
Usage: grails generate-mxml <mxml file name>

Creates an MXML file using the specified name and location.

Example: grails generate-mxml web-app/Main.mxml
"""

target(generateMxml: 'Creates an MXML file') {
	depends(checkVersion, configureProxy)

	args = args ? args.split('\n') : []

	File mxml
	switch (args.size()) {
		case 1:
			mxml = new File(new File(args[0]).canonicalPath)
			break
		default:
			ant.echo message: USAGE
			exit 1
			break
	}

	mxml.parentFile.mkdirs()
	File history = new File(mxml.parentFile, 'history')
	history.mkdirs()

	copyFile "$pluginDirPath/src/resources/mxml/mxml.template", mxml.path, true

	copyFile "$pluginDirPath/src/resources/mxml/playerProductInstall.swf",
	         new File(history, 'playerProductInstall.swf').path, true

	copyFile "$pluginDirPath/src/resources/mxml/swfobject.js",
	         new File(history, 'swfobject.js').path, true

	copyFile "$pluginDirPath/src/resources/mxml/history.js",
	         new File(history, 'history.js').path, true

	copyFile "$pluginDirPath/src/resources/mxml/history.css",
	         new File(history, 'history.css').path, true

	copyFile "$pluginDirPath/src/resources/mxml/historyFrame.html",
	         new File(history, 'historyFrame.html').path, true
}

setDefaultTarget 'generateMxml'
