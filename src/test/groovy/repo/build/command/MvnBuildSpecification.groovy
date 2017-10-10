package repo.build.command

import repo.build.Maven
import repo.build.MavenFeature
import repo.build.RepoBuild
import spock.lang.Specification

class MvnBuildSpecification extends Specification {

    def setup() {
        GroovyMock(MavenFeature, global: true)
        GroovyMock(Maven, global: true)
    }

    def "without args"() {
        def repoBuild = new RepoBuild('mvn-execute')
        def basedir = new File('.').getAbsoluteFile()

        when:
        repoBuild.execute()

        then:
        1 * MavenFeature.buildParents(_)
        1 * Maven.execute(_, new File(basedir, 'pom.xml'), ['clean', 'install'], [:])
    }

    def "with args"() {
        def repoBuild = new RepoBuild('mvn-execute', '-j', '2', '-DskipTests', '-Dopt1=value1')
        def basedir = new File('.').getAbsoluteFile()

        when:
        repoBuild.execute()

        then:
        1 * MavenFeature.buildParents(_)
        1 * Maven.execute(_, new File(basedir, 'pom.xml'), ['clean', 'install'], ['skipTests': 'true', 'opt1': 'value1'])
    }
}
