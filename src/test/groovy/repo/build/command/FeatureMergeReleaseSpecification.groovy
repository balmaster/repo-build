package repo.build.command

import repo.build.Git
import repo.build.GitFeature
import repo.build.Pom
import repo.build.RepoBuild
import repo.build.RepoBuildException
import spock.lang.Specification

class FeatureMergeReleaseSpecification extends Specification {

    def setup() {
        GroovyMock(GitFeature, global: true)
    }

    def "without args"() {
        def repoBuild = new RepoBuild('feature-merge-release')

        when:
        repoBuild.execute()

        then:
        thrown(RepoBuildException)
    }

    def "with featureBranch"() {
        def repoBuild = new RepoBuild('feature-merge-release', '-f', 'feature/1')

        when:
        repoBuild.execute()

        then:
        1 * GitFeature.mergeRelease(_, 'feature/1')
    }

    def "with parallel with featureBranch"() {
        def repoBuild = new RepoBuild('feature-merge-release', '-j', '2', '-f', 'feature/1')

        when:
        repoBuild.execute()

        then:
        1 * GitFeature.mergeRelease(_, 'feature/1')
    }

}
