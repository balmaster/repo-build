package repo.build.command

import repo.build.GitFeature
import repo.build.RepoBuild
import repo.build.RepoBuildException
import spock.lang.Specification

class StashSpecification extends Specification {

    def setup() {
        GroovyMock(GitFeature, global: true)
    }

    def "without args"() {
        def repoBuild = new RepoBuild('stash')

        when:
        repoBuild.execute()

        then:
        1 * GitFeature.stash(_, 1)
    }

    def "with args"() {
        def repoBuild = new RepoBuild('stash', '-j', '2')

        when:
        repoBuild.execute()

        then:
        1 * GitFeature.stash(_, 2)
    }

}
