package repo.build.command

import repo.build.GitFeature
import repo.build.RepoBuild
import repo.build.RepoBuildException
import spock.lang.Specification

class ReleaseMergeReleaseSpecification extends Specification {

    def setup() {
        GroovyMock(GitFeature, global: true)
    }

    def "without args"() {
        def repoBuild = new RepoBuild('release-merge-release')

        when:
        repoBuild.execute()

        then:
        thrown(RepoBuildException)
    }

    def "with release-source"() {
        def repoBuild = new RepoBuild('release-merge-feature', '-rs', '1.0')

        when:
        repoBuild.execute()

        then:
        thrown(RepoBuildException)
    }

    def "with release-destination"() {
        def repoBuild = new RepoBuild('release-merge-feature', '-rd', '1.0')

        when:
        repoBuild.execute()

        then:
        thrown(RepoBuildException)
    }

    def "with release-regexp"() {
        def repoBuild = new RepoBuild('release-merge-release', '-rr', 'regexp')

        when:
        repoBuild.execute()

        then:
        thrown(RepoBuildException)
    }

    def "with release-destination and release-source"() {
        def repoBuild = new RepoBuild('release-merge-release', '-rs', '1.0', '-rd', '1.1')

        when:
        repoBuild.execute()

        then:
        1 * GitFeature.releaseMergeRelease(_, '1.0', '1.1', /(\d+\.\d+)/, _)
    }

    def "with release-source, release-destination, release-regexp"() {
        def repoBuild = new RepoBuild('release-merge-release', '-rs', '1.0', '-rd', '1.1', '-rr', 'regexp')

        when:
        repoBuild.execute()

        then:
        1 * GitFeature.releaseMergeRelease(_, '1.0', '1.1', 'regexp', _)
    }

}
