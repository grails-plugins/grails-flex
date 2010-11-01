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
 * @author Bill Bejeck
 * @author <a href='mailto:burt@burtbeckwith.com'>Burt Beckwith</a>
 */

import grails.util.Environment
import grails.util.GrailsUtil

includeTargets << new File("$flexPluginDir/scripts/_FlexCommon.groovy")

ant.taskdef resource: 'flexTasks.tasks', classpath: "$flexHome/ant/lib/flexTasks.jar"

target(mxmlcInit: 'TODO') {
	depends(checkVersion, configureProxy, packageApp)

	if (!flexHome) {
		println "\nERROR: Cannot compile since FLEX_HOME isn't set. Either set as the FLEX_HOME environment variable or the 'grails.plugin.flex.home' property in Config.groovy\n"
		exit 1
	}

	args = args ? args.split('\n') : []
}

splitFile = { String path ->
	def file = new File(path)
	def parentFile = (file.parentFile ?: new File(basedir)).canonicalFile
	[dir: parentFile.path, name: file.name]
}

compileMxmlc = { File mxml, File swf ->
	try {
		String contextRoot = '/'
		if (flexConfig.mxmlc.contextRoot instanceof CharSequence) {
			contextRoot = flexConfig.mxmlc.contextRoot.toString()
		}
		else if (Environment.current == Environment.DEVELOPMENT) {
			contextRoot = metadata['app.name']
		}

		def mxmlcParams = [file: mxml.path, output: swf.path, fork: true,
		                   services: "$basedir/web-app/WEB-INF/flex/services-config.xml",
		                   'show-unused-type-selector-warnings': false,
		                   'show-invalid-css-property-warnings': false,
		                   'context-root': contextRoot]

		if (flexConfig.mxmlc.actionscriptFileEncoding instanceof CharSequence) {
			mxmlcParams['actionscript-file-encoding'] = flexConfig.mxmlc.actionscriptFileEncoding.toString()
		}
		if (flexConfig.mxmlc.keepGenerated instanceof Boolean) {
			mxmlcParams['keep-generated-actionscript'] = flexConfig.mxmlc.keepGenerated
		}
		if (flexConfig.mxmlc.incremental instanceof Boolean) {
			mxmlcParams.incremental = flexConfig.mxmlc.incremental
		}
		if (flexConfig.mxmlc.debug instanceof Boolean) {
			mxmlcParams.debug = flexConfig.mxmlc.debug
		}
		if (flexConfig.mxmlc.verboseStacktraces instanceof Boolean) {
			mxmlcParams['verbose-stacktraces'] = flexConfig.mxmlc.verboseStacktraces
		}
		if (flexConfig.mxmlc.accessible instanceof Boolean) {
			mxmlcParams.accessible = flexConfig.mxmlc.accessible
		}
		if (flexConfig.mxmlc.headless instanceof Boolean) {
			mxmlcParams['headless-server'] = flexConfig.mxmlc.headless
		}
		if (flexConfig.mxmlc.optimize instanceof Boolean) {
			mxmlcParams.optimize = flexConfig.mxmlc.optimize
		}

		ant.mxmlc(mxmlcParams) {

			'load-config'(filename: "$basedir/web-app/WEB-INF/flex/flex-config.xml")

			'source-path'('path-element': "$basedir/web-app/WEB-INF/flex")
			if (flexConfig.mxmlc.extraSourcePaths instanceof List) {
				for (path in flexConfig.mxmlc.extraSourcePaths) {
					'source-path'('path-element': path)
				}
			}

			'compiler.library-path'(dir: "$basedir/web-app/WEB-INF/flex", append: true) {
				include name: 'libs'
				include name: '../bundles/{locale}'
			}

			'compiler.library-path'(dir: "$basedir/web-app/WEB-INF/flex", append: true) {
				include name: 'user_classes'
			}

			if (flexConfig.mxmlc.extraLibPaths instanceof List) {
				for (path in flexConfig.mxmlc.extraLibPaths) {
					'compiler.library-path'(dir: splitFile(path).dir, append: true) {
						include name: splitFile(path).name
					}
				}
			}

			if (flexConfig.mxmlc.compileTimeConstants instanceof Map) {
				flexConfig.mxmlc.compileTimeConstants.each { k, v ->
					define name: k, value: v
				}
			}
		}
	}
	catch (Throwable t) {
		GrailsUtil.sanitize t
		t.printStackTrace()
		throw t
	}
}

generateGsp = { File swf, File gsp ->
	try {
		def wrapperConfig = flexConfig.mxmlc.htmlWrapper

		boolean history = true
		if (wrapperConfig.history instanceof Boolean) {
			history = wrapperConfig.history
		}

		String shortName = swf.name[0..-5]
		def wrapperParams = [title: wrapperConfig.title ?: shortName,
									file: gsp.name,
									height: wrapperConfig.height ?: '100%',
									width: wrapperConfig.width ?: '100%',
									bgcolor: wrapperConfig.bgcolor ?: '#ffffff',
									application: shortName, swf: shortName,
									'version-major': wrapperConfig.version.major ?: '10',
									'version-minor': wrapperConfig.version.minor ?: '0',
									'version-revision': wrapperConfig.version.revision ?: '0',
									history: history,
									output: gsp.parentFile]

		ant.echo message: "\nGenerating $gsp.name\n"
		ant.'html-wrapper'(wrapperParams)
	}
	catch (Throwable t) {
		GrailsUtil.sanitize t
		t.printStackTrace()
		throw t
	}
}
