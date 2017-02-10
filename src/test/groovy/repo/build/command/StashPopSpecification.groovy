package repo.build.command

import repo.build.GitFeature
import repo.build.RepoBuild
import spock.lang.Specification

class StashPopSpecification extends Specification {

    def setup() {
        GroovyMock(GitFeature, global: true)
    }

    def "without args"() {
        def repoBuild = new RepoBuild('stash-pop')

        when:
        repoBuild.execute()

        then:
        1 * GitFeature.stashPop(_)
    }

}
