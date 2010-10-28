grails.project.class.dir = 'target/classes'
grails.project.test.class.dir = 'target/test-classes'
grails.project.test.reports.dir = 'target/test-reports'

grails.project.dependency.resolution = {

	inherits 'global'

	log 'warn'

	repositories {
		grailsPlugins()
		grailsHome()
		grailsCentral()

		mavenRepo 'http://repository.sonatype.org/content/groups/flexgroup' // flex
		ebr() // SpringSource  http://www.springsource.com/repository
		mavenCentral()
	}

	dependencies {
		runtime 'org.apache.xalan:com.springsource.org.apache.xml.serializer:2.7.1'
//		runtime 'com.adobe.flex.framework:flex-framework:4.1.0.16248'
	}
}
//<dependency>
//<groupId>org.springframework.integration</groupId>
//<artifactId>spring-integration-core</artifactId>
//</dependency>
