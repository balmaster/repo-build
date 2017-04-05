package repo.build.command

import repo.build.GitFeature
import repo.build.RepoBuild
import repo.build.RepoBuildException
import spock.lang.Specification

class ReleaseMergeFeatureSpecification extends Specification {

    def setup() {
        GroovyMock(GitFeature, global: true)
    }

    def "without args"() {
        def repoBuild = new RepoBuild('release-merge-feature')

        when:
        repoBuild.execute()

        then:
        thrown(RepoBuildException)
    }

    def "with featureBranch"() {
        def repoBuild = new RepoBuild('release-merge-feature', '-f', 'feature/1')

        when:
        repoBuild.execute()

        then:
        1 * GitFeature.mergeFeature(_, 1, 'feature/1')
    }

    def "with parallel with featureBranch"() {
        def repoBuild = new RepoBuild('release-merge-feature', '-j', '2', '-f', 'feature/1')

        when:
        repoBuild.execute()

        then:
        1 * GitFeature.mergeFeature(_, 2, 'feature/1')
    }

}
