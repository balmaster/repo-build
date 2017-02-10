package repo.build.command

import repo.build.GitFeature
import repo.build.RepoBuild
import spock.lang.Specification

class SyncSpecification extends Specification {

    def setup() {
        GroovyMock(GitFeature, global: true)
    }

    def "without args"() {
        def repoBuild = new RepoBuild('sync')

        when:
        repoBuild.execute()

        then:
        1 * GitFeature.sync(_)
    }

}
