package repo.build.command

import repo.build.Maven
import repo.build.MavenFeature
import repo.build.RepoBuild
import spock.lang.Specification

class MvnBuildSpecification extends Specification {

    def setup() {
        GroovyMock(MavenFeature, global: true)
    }

    def "without args"() {
        def repoBuild = new RepoBuild('mvn-execute')

        when:
        repoBuild.execute()

        then:
        1 * MavenFeature.build(_, new File(new File('.').getAbsoluteFile(), 'parent/pom.xml'), ['clean', 'install'], [:])
        1 * Maven.build(_, new File(new File('.').getAbsoluteFile(), 'pom.xml'), ['clean', 'install'], [:])
    }

    def "with args"() {
        def repoBuild = new RepoBuild('mvn-execute', '-j', '2', '-DskipTests', '-Dopt1=value1')

        when:
        repoBuild.execute()

        then:
        1 * MavenFeature.build(_, new File(new File('.').getAbsoluteFile(), 'parent/pom.xml'), ['clean', 'install'], ['skipTests': 'true', 'opt1': 'value1'])
        1 * Maven.build(_, new File(new File('.').getAbsoluteFile(), 'pom.xml'), ['clean', 'install'], ['skipTests': 'true', 'opt1': 'value1'])
    }
}
