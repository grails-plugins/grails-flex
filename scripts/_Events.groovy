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

eventCreateWarStart = { name, stagingDir ->

	// default to precompile true
	if (flexConfig.precompileMxml.enabled instanceof Boolean && !flexConfig.precompileMxml.enabled) {
		return
	}

	mxmlcInit()

	ant.delete dir: "$basedir/target/swf"
	ant.mkdir dir: "$basedir/target/swf"

	def precompileMxmlFiles
	if (flexConfig.precompileMxml.files instanceof List) {
		precompileMxmlFiles = []
		for (mxmlName in flexConfig.precompileMxml.files) {
			precompileMxmlFiles << new File(stagingDir, mxmlName)
		}
	}
	else {
		precompileMxmlFiles = findAllMxmlFiles()
	}

	boolean createGsp = flexConfig.precompileMxml.htmlWrapper.create instanceof Boolean &&
		flexConfig.precompileMxml.htmlWrapper.create

	for (file in precompileMxmlFiles) {
		String path = file.absoluteFile.path - stagingDir.absoluteFile.path
		if (path[0] == '/' || path[0] == '\\') {
			path = path[1..-1]
		}

		ant.echo message: "\nPrecompiling MXML $path\n"
		path = path[0..-6] // remove .mxml
		File swf = new File("$basedir/target/swf/${path}.swf")
		File mxml = new File(stagingDir, "${path}.mxml")
		compileMxmlc mxml, swf

		if (createGsp) {
			String extension = flexConfig.mxmlc.htmlWrapper.extension ?: 'gsp'
			File gsp = new File("$basedir/target/swf", "${path}.${extension}")
			generateGsp swf, gsp
		}
	}

	String views = "$stagingDir/WEB-INF/grails-app/views"
	ant.mkdir dir: views
	ant.move(todir: views) {
		fileset dir: "$basedir/target/swf", includes: '**/*.gsp'
	}

	ant.copy(todir: stagingDir) {
		fileset dir: "$basedir/target/swf", excludes: 'generated/**'
	}
}

private List findAllMxmlFiles() {
	def files = []
	stagingDir.eachFileRecurse { file ->
		if (!file.directory && file.name.toLowerCase().endsWith('.mxml')) {
			files << file
		}
	}
	files
}
