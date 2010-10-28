includeTargets << new File("$flexPluginDir/scripts/_FlexCommon.groovy")

eventCreateWarStart = { name, stagingDir ->

	// default to precompile true
	if (flexConfig.precompileMxml.enabled instanceof Boolean && !flexConfig.precompileMxml.enabled) {
		return
	}

	if (!flexHome) {
		println "\nERROR: FLEX_HOME isn't set. Either set as the FLEX_HOME environment variable or the 'grails.plugin.flex.home' property in Config.groovy - skipping mxml compilation\n"
		return
	}

	ant.taskdef resource: 'flexTasks.tasks', classpath: "$flexHome/ant/lib/flexTasks.jar"

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

	for (file in precompileMxmlFiles) {
		String path = file.absoluteFile.path - stagingDir.absoluteFile.path
		if (path[0] == '/' || path[0] == '\\') {
			path = path[1..-1]
		}
		path = path[0..-6] // remove .mxml
		println "\nPrecompiling MXML $path\n"
		try {
			compileMxml stagingDir, path
		}
		catch (Throwable t) {
			t.printStackTrace()
			throw t
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

compileMxml = { stagingDir, String name ->

	File swf = new File("$basedir/target/swf/${name}.swf")

	def mxmlcParams = [file: "$stagingDir/${name}.mxml",
	                   output: swf.path,
	                   fork: true,
	                   services: "$basedir/web-app/WEB-INF/flex/services-config.xml"]
	if (flexConfig.precompileMxml.contextRoot instanceof CharSequence) {
		mxmlcParams['context-root'] = flexConfig.precompileMxml.contextRoot.toString()
	}
	if (flexConfig.precompileMxml.actionscriptFileEncoding instanceof CharSequence) {
		mxmlcParams['actionscript-file-encoding'] = flexConfig.precompileMxml.actionscriptFileEncoding.toString()
	}
	if (flexConfig.precompileMxml.keepGenerated instanceof Boolean) {
		mxmlcParams['keep-generated-actionscript'] = flexConfig.precompileMxml.keepGenerated
	}
	if (flexConfig.precompileMxml.incremental instanceof Boolean) {
		mxmlcParams.incremental = flexConfig.precompileMxml.incremental
	}
	if (flexConfig.precompileMxml.debug instanceof Boolean) {
		mxmlcParams.debug = flexConfig.precompileMxml.debug
	}
	if (flexConfig.precompileMxml.verboseStacktraces instanceof Boolean) {
		mxmlcParams['verbose-stacktraces'] = flexConfig.precompileMxml.verboseStacktraces
	}

	ant.mxmlc(mxmlcParams) {

		'load-config'(filename: "$stagingDir/WEB-INF/flex/flex-config.xml")

		'source-path'('path-element': "$stagingDir/WEB-INF/flex")
		
		'compiler.library-path'(dir: "$stagingDir/WEB-INF/flex", append: true) {
			include name: 'libs'
			include name: '../bundles/{locale}'
		}

		'compiler.library-path'(dir: "$stagingDir/WEB-INF/flex", append: true) {
			include name: 'user_classes'
		}

		if (flexConfig.precompileMxml.extraLibPaths instanceof List) {
			for (path in flexConfig.precompileMxml.extraLibPaths) {
				'compiler.library-path'(dir: path, append: true) {
					include name: 'libs'
					include name: 'locale'
				}
			}
		}
	}

	if (flexConfig.precompileMxml.htmlWrapper.create instanceof Boolean && flexConfig.precompileMxml.htmlWrapper.create) {
		String extension = flexConfig.precompileMxml.htmlWrapper.extension ?: 'gsp'
		boolean history = true
		if (flexConfig.precompileMxml.htmlWrapper.history instanceof Boolean) {
			history = flexConfig.precompileMxml.htmlWrapper.history 
		}

		String shortName = new File(name).name
		def wrapperParams = [title: shortName, // TODO
		                     file: name + '.' + extension,
		                     height: flexConfig.precompileMxml.htmlWrapper.height ?: '100%',
		                     width: flexConfig.precompileMxml.htmlWrapper.width ?: '100%',
		                     bgcolor: flexConfig.precompileMxml.htmlWrapper.bgcolor ?: '#ffffff',
		                     application: shortName, swf: shortName,
		                     'version-major': flexConfig.precompileMxml.htmlWrapper.version.major ?: '10',
		                     'version-minor': flexConfig.precompileMxml.htmlWrapper.version.minor ?: '0',
		                     'version-revision': flexConfig.precompileMxml.htmlWrapper.version.revision ?: '0',
		                     history: history,
		                     output: "$basedir/target/swf"]
		ant.'html-wrapper'(wrapperParams)

		ant.copy file: "$flexHome/templates/swfobject/playerProductInstall.swf",
		         todir: new File(stagingDir, name).parentFile
		ant.copy file: "$flexHome/templates/swfobject/swfobject.js",
		         todir: new File(stagingDir, name).parentFile
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
