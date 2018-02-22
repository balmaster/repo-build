package repo.build.command

import repo.build.MavenFeature
import repo.build.RepoBuild
import spock.lang.Specification

class MvnBuildParallelSpecification extends Specification {

    def setup() {
        GroovyMock(MavenFeature, global: true)
    }

    def "without args"() {
        def repoBuild = new RepoBuild('mvn-build-parallel')

        when:
        repoBuild.execute()

        then:
        1 * MavenFeature.buildParallel(_)
    }

}
