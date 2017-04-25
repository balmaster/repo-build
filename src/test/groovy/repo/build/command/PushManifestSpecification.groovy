package repo.build.command

import repo.build.GitFeature
import repo.build.RepoBuild
import repo.build.RepoBuildException
import spock.lang.Specification

class PushManifestSpecification extends Specification {

    def setup() {
        GroovyMock(GitFeature, global: true)
    }

    def "without args"() {
        def repoBuild = new RepoBuild('push-manifest')

        when:
        repoBuild.execute()

        then:
        1 * GitFeature.pushManifestBranch(_, true)
    }

    def "with parallel args"() {
        def repoBuild = new RepoBuild('push-manifest', '-j', '2')

        when:
        repoBuild.execute()

        then:
        1 * GitFeature.pushManifestBranch(_, true)
    }

}
