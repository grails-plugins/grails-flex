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
Usage: grails compile-mxml <mxml file name> [swf file name] [GSP wrapper file name]

Compiles the specified .mxml file into a .swf, either to the same directory
if no output file is specified or to the specified output file if it is.
Also generates an HTML wrapper file if the third parameter is specified.

Example: grails compile-mxml web-app/Main.mxml
Example: grails compile-mxml web-app/flex/Main.mxml web-app/Main.swf
Example: grails compile-mxml web-app/flex/Main.mxml web-app/Main.swf grails-app/views/Main.gsp
"""

target(compileMxml: 'Compile a .mxml file and its includes into a .swf file') {
	depends(mxmlcInit)

	File mxml
	File swf
	File gsp
	switch (args.size()) {
		case 1:
			String mxmlName = args[0]
			mxml = new File(mxmlName)
			swf = new File(mxmlName[0..-6] + '.swf')
			break
		case 2:
			mxml = new File(args[0])
			swf = new File(args[1])
			break
		case 3:
			mxml = new File(args[0])
			swf = new File(args[1])
			gsp = new File(args[2])
			break
		default:
			ant.echo message: USAGE
			exit 1
			break
	}

	if (!mxml.exists()) {
		ant.echo message: "ERROR: mxml file $mxml.path not found"
		exit 1
	}

	compileMxmlc new File(mxml.canonicalPath), new File(swf.canonicalPath)
	if (gsp) {
		generateGsp new File(swf.canonicalPath), new File(gsp.canonicalPath)
	}
}

setDefaultTarget 'compileMxml'
