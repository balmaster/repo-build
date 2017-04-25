package repo.build.command

import repo.build.GitFeature
import repo.build.RepoBuild
import repo.build.RepoBuildException
import spock.lang.Specification

class PrepareMergeSpecification extends Specification {

    def setup() {
        GroovyMock(GitFeature, global: true)
    }

    def "without args"() {
        def repoBuild = new RepoBuild('prepare-merge')

        when:
        repoBuild.execute()

        then:
        thrown(RepoBuildException)
    }

    def "with featureBranch"() {
        def repoBuild = new RepoBuild('prepare-merge', '-f', 'feature/1')

        when:
        repoBuild.execute()

        then:
        1 * GitFeature.mergeFeature(_, 'feature/1', false)
    }

    def "with parallel with featureBranch"() {
        def repoBuild = new RepoBuild('prepare-merge', '-j', '2', '-f', 'feature/1')

        when:
        repoBuild.execute()

        then:
        1 * GitFeature.mergeFeature(_, 'feature/1', false)
    }

    def "with featureBranch and abort flag"() {
        def repoBuild = new RepoBuild('prepare-merge', '-f', 'feature/1', '-a')

        when:
        repoBuild.execute()

        then:
        1 * GitFeature.mergeFeature(_, 'feature/1', true)
    }
}
