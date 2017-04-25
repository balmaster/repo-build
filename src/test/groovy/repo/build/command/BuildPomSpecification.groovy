package repo.build.command

import repo.build.Git
import repo.build.Pom
import repo.build.RepoBuild
import spock.lang.Specification

class BuildPomSpecification extends Specification {
    def dir = new File(".").getAbsoluteFile()

    def setup() {
        GroovyMock(Git, global: true)
        GroovyMock(Pom, global: true)
    }

    def "without args"() {
        def repoBuild = new RepoBuild('build-pom')

        setup:
        1 * Git.getBranch(_, new File(dir, 'manifest')) >> '1.0'

        when:
        repoBuild.execute()

        then:
        1 * Pom.generateXml(_, "${dir.parentFile.name}-1.0", new File(dir, 'pom.xml'))
    }

    def "with featureBranch"() {
        def repoBuild = new RepoBuild('build-pom', '-f', 'feature/1')

        when:
        repoBuild.execute()

        then:
        1 * Pom.generateXml(_, "${dir.parentFile.name}-feature/1", new File(dir, 'pom.xml'))
    }

}
