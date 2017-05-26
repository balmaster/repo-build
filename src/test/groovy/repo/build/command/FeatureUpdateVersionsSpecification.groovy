package repo.build.command

import repo.build.MavenFeature
import repo.build.RepoBuild
import repo.build.RepoBuildException
import spock.lang.Specification

class FeatureUpdateVersionsSpecification extends Specification {

    def setup() {
        GroovyMock(MavenFeature, global: true)
    }

    def "without args"() {
        def repoBuild = new RepoBuild('feature-update-versions')

        when:
        repoBuild.execute()

        then:
        thrown(RepoBuildException)
    }

    def "without includes"() {
        def repoBuild = new RepoBuild('feature-update-versions', '-f', 'feature/1')

        when:
        repoBuild.execute()

        then:
        thrown(RepoBuildException)
    }

    def "without featureBranch"() {
        def repoBuild = new RepoBuild('feature-update-versions', '-i', 'group')

        when:
        repoBuild.execute()

        then:
        thrown(RepoBuildException)
    }

    def "with featureBranch with includes"() {
        def repoBuild = new RepoBuild('feature-update-versions', '-f', 'feature/1', '-i', 'group')

        when:
        repoBuild.execute()

        then:
        1 * MavenFeature.updateVersions(_, 'feature/1', 'group', null, true, _)
    }

    def "with featureBranch with includes continue"() {
        def repoBuild = new RepoBuild('feature-update-versions', '-f', 'feature/1', '-i', 'group', '-C', 'component')

        when:
        repoBuild.execute()

        then:
        1 * MavenFeature.updateVersions(_, 'feature/1', 'group', 'component', true, _)
    }

}
