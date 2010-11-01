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

includeTargets << new File("$flexPluginDir/scripts/_MxmlcTasks.groovy")

USAGE = """
Usage: grails generate-swf-gsp <swf file name> [gsp file name]

Generates a wrapper html file for the specified .swf file, either to the same directory
if no output file is specified or to the specified output file if it is.

Example: grails generate-swf-gsp web-app/Main.swf
Example: grails generate-swf-gsp web-app/Main.swf grails-app/views/Main.gsp
"""

target(generateSwfGsp: 'Creates a gsp wrapper to load the flash player') {
	depends(mxmlcInit)

	File gsp
	File swf
	switch (args.size()) {
		case 1:
			String swfName = args[0]
			swf = new File(swfName)
			String extension = flexConfig.mxmlc.htmlWrapper.extension ?: 'gsp'
			gsp = new File(swfName[0..-5] + '.' + extension)
			break
		case 2:
			swf = new File(args[0])
			gsp = new File(args[1])
			break
		default:
			ant.echo message: USAGE
			exit 1
			break
	}

	if (!swf.exists()) {
		ant.echo message: "ERROR: swf file $swf.path not found"
		exit 1
	}

	generateGsp new File(swf.canonicalPath), new File(gsp.canonicalPath)
}

setDefaultTarget 'generateSwfGsp'
