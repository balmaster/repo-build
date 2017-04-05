package repo.build.command

import repo.build.GitFeature
import repo.build.RepoBuild
import repo.build.RepoBuildException
import spock.lang.Specification

class SwitchSpecification extends Specification {

    def setup() {
        GroovyMock(GitFeature, global: true)
    }

    def "without args"() {
        def repoBuild = new RepoBuild('switch')

        when:
        repoBuild.execute()

        then:
        thrown(RepoBuildException)
    }

    def "with featureBranch"() {
        def repoBuild = new RepoBuild('switch', '-f', 'feature/1', '-j', '2')

        when:
        repoBuild.execute()

        then:
        1 * GitFeature.switch(_, 2, 'feature/1')
    }

}
