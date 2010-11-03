/**
 * Copyright 2007-2010 the original author or authors.
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
import grails.util.Environment

class FlexGrailsPlugin {

	String version = '0.4'
	String grailsVersion = '1.2.2 > *'
	List loadAfter = ['services', 'hibernate']
	List observe = ['services']
	String author = 'Graeme Rocher'
	String authorEmail = 'graeme.rocher@springsource.com'
	String title = 'Provides integration between Grails and Flex'
	String description = 'Provides integration between Grails and Flex'
	String documentation = 'http://grails.org/plugins/flex'
	Map dependsOn = ['blazeds': '1.1 > *']
	List pluginExcludes = [
		'web-app/WEB-INF/**',
		'grails-app/domain/**',
		'docs/**',
		'src/docs/**'
	]

	def doWithWebDescriptor = { xml ->
		def flatConfig = application.config.flatten()
		def flexConfig = application.config.grails.plugin.flex
		boolean webtierCompilerEnabled = flatConfig.containsKey('grails.plugin.flex.webtier.compiler.enabled') ?
				flexConfig.webtier.compiler.enabled :
				Environment.DEVELOPMENT == Environment.current

		// context params
		def contextParams = xml.'context-param'
		contextParams[contextParams.size() - 1] + {
		'context-param' {
			'param-name'('flex.class.path')
				'param-value'("/WEB-INF/flex/hotfixes,/WEB-INF/flex/jars")
			}
		}

		// servlets
		def servlets = xml.servlet
		servlets[servlets.size() - 1] + {

			servlet {
				'servlet-name'('FlexForbiddenServlet')
				'display-name'('Prevents access to *.as/*.swc files')
				'servlet-class'('flex.bootstrap.BootstrapServlet')
				'init-param' {
					'param-name'('servlet.class')
					'param-value'('flex.webtier.server.j2ee.ForbiddenServlet')
				}
			}

			if (webtierCompilerEnabled) {
				servlet {
					'servlet-name'('FlexMxmlServlet')
					'display-name'('MXML Processor')
					'description'('Servlet wrapper for the Mxml Compiler')
					'servlet-class'('flex.bootstrap.BootstrapServlet')
					'init-param' {
						'param-name'('servlet.class')
						'param-value'('flex.webtier.server.j2ee.MxmlServlet')
					}
					'init-param' {
						'param-name'('webtier.configuration.file')
						'param-value'('/WEB-INF/flex/flex-webtier-config.xml')
					}
					'load-on-startup'('1')
				}

				servlet {
					'servlet-name'('FlexSwfServlet')
					'display-name'('SWF Retriever')
					'servlet-class'('flex.bootstrap.BootstrapServlet')
					'init-param' {
						'param-name'('servlet.class')
						'param-value'('flex.webtier.server.j2ee.SwfServlet')
					}
					'load-on-startup'('2')
				}
			}
		}

		// servlet mappings
		def servletMappings = xml.'servlet-mapping'
		servletMappings[servletMappings.size() - 1] + {

			'servlet-mapping' {
				'servlet-name'('FlexForbiddenServlet')
				'url-pattern'('*.as')
			}

			'servlet-mapping' {
				'servlet-name'('FlexForbiddenServlet')
				'url-pattern'('*.swc')
			}

			if (webtierCompilerEnabled) {
				'servlet-mapping' {
					'servlet-name'('FlexMxmlServlet')
					'url-pattern'('*.mxml')
				}

				'servlet-mapping' {
					'servlet-name'('FlexSwfServlet')
					'url-pattern'('*.swf')
				}
			}
			else {
				'servlet-mapping' {
					'servlet-name'('FlexForbiddenServlet')
					'url-pattern'('*.mxml')
				}
			}
		}
	}
}
