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

target(flexInit: 'Copies files from FLEX_HOME') {
	depends(checkVersion, configureProxy)

	if (!flexHome) {
		println "\nERROR: FLEX_HOME isn't set. Either set as the FLEX_HOME environment variable or the 'grails.plugin.flex.home' property in Config.groovy\n"
		exit 1
	}

	String flexDir = "$basedir/web-app/WEB-INF/flex"

	// TODO ask if files already exist

	ant.mkdir dir: "$flexDir/jars"
	ant.copy(todir: "$flexDir/jars", verbose: true) {
		fileset(dir: "$flexHome/lib") {
			include name: 'asc.jar'
			include name: 'batik-all-flex.jar'
			include name: 'flex-fontkit.jar'
			include name: 'flex-messaging-common.jar'
			include name: 'fxgutils.jar'
			include name: 'license.jar'
			include name: 'mxmlc.jar'
			include name: 'swfutils.jar'
			include name: 'velocity-dep-1.4-flex.jar'
			include name: 'xercesImpl.jar'
			include name: 'xercesPatch.jar'
		}
	}
	ant.copy(todir: "$flexDir/jars", verbose: true) {
		fileset(dir: "$flexPluginDir/src/resources/jars") {
			include name: 'oscache.jar'
			include name: 'flex-webtier.jar'
			include name: 'flex-webtier-jsp.jar'
		}
	}

	['libs', 'locale', 'themes'].each { dir ->
		ant.mkdir dir: "$flexDir/$dir"
		ant.copy(todir: "$flexDir/$dir") {
			fileset dir: "$flexHome/frameworks/$dir"
		}
	}

	ant.copy(todir: "$flexDir", verbose: true) {
		fileset(dir: "$flexHome/frameworks") {
			include name: '*Fonts.ser'
			include name: '*-manifest.xml'
			include name: 'air-config.xml'
			include name: 'flash-unicode-table.xml'
		}
	}

	ant.copy(todir: "$flexDir", verbose: true) {
		fileset(dir: "$flexPluginDir/src/resources") {
			include name: 'flex-config.xml'
			include name: 'flex-webtier-config.xml'
		}
	}

	ant.mkdir dir: "$flexDir/user_classes"
	new File("$flexDir/user_classes", 'add_your_as_and_swc_files_here.txt').createNewFile()
}

setDefaultTarget 'flexInit'
