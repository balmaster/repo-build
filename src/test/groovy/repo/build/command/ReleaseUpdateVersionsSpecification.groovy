package repo.build.command

import repo.build.MavenFeature
import repo.build.RepoBuild
import repo.build.RepoBuildException
import spock.lang.Specification

class ReleaseUpdateVersionsSpecification extends Specification {

    def setup() {
        GroovySpy(MavenFeature, global: true)
    }

    def "without args"() {
        def repoBuild = new RepoBuild('release-update-versions')

        when:
        repoBuild.execute()

        then:
        thrown(RepoBuildException)
    }

    def "without includes"() {
        def repoBuild = new RepoBuild('release-update-versions', '-f', 'feature/1')

        when:
        repoBuild.execute()

        then:
        thrown(RepoBuildException)
    }

    def "without featureBranch"() {
        def repoBuild = new RepoBuild('release-update-versions', '-i', 'group')

        when:
        repoBuild.execute()

        then:
        thrown(RepoBuildException)
    }

    def "with featureBranch with includes"() {
        GroovyMock(MavenFeature, global: true)
        def repoBuild = new RepoBuild('release-update-versions', '-i', 'group')

        when:
        repoBuild.execute()

        then:
        1 * MavenFeature.releaseUpdateVersions(_, 'group', null)
    }

    def "with featureBranch with includes continue"() {
        GroovyMock(MavenFeature, global: true)
        def repoBuild = new RepoBuild('release-update-versions', '-i', 'group', '-C', 'component')

        when:
        repoBuild.execute()

        then:
        1 * MavenFeature.releaseUpdateVersions(_, 'group', 'component')
    }

}
