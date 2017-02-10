package repo.build.command

import repo.build.GitFeature
import repo.build.RepoBuild
import repo.build.RepoBuildException
import spock.lang.Specification

class MergeAbortSpecification extends Specification {

    def setup() {
        GroovyMock(GitFeature, global: true)
    }

    def "without args"() {
        def repoBuild = new RepoBuild('merge-abort')

        when:
        repoBuild.execute()

        then:
        1 * GitFeature.mergeAbort(_)

    }

}
