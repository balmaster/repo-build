package repo.build.command

import repo.build.MavenFeature
import repo.build.RepoBuild
import repo.build.RepoBuildException
import spock.lang.Specification

class ReleaseUpdateParentSpecification extends Specification {

    def setup() {
        GroovyMock(MavenFeature, global: true)
    }

    def "without args"() {
        def repoBuild = new RepoBuild('release-update-parent')

        when:
        repoBuild.execute()

        then:
        1 * MavenFeature.updateReleaseParent(_, 'parent', true, false)
    }

    def "without parent"() {
        def repoBuild = new RepoBuild('release-update-parent', '-f', 'feature/1')

        when:
        repoBuild.execute()

        then:
        1 * MavenFeature.updateReleaseParent(_, 'parent', true, false)
    }

    def "without featureBranch"() {
        def repoBuild = new RepoBuild('release-update-parent', '-P', 'parent1')

        when:
        repoBuild.execute()

        then:
        1 * MavenFeature.updateReleaseParent(_, 'parent1', true, false)
    }

    def "with parent"() {
        def repoBuild = new RepoBuild('release-update-parent', '-P', 'parent1')

        when:
        repoBuild.execute()

        then:
        1 * MavenFeature.updateReleaseParent(_, 'parent1', true, false)
    }

    def "with parallel with parent"() {
        def repoBuild = new RepoBuild('release-update-parent', '-j', '2', '-P', 'parent1')

        when:
        repoBuild.execute()

        then:
        1 * MavenFeature.updateReleaseParent(_, 'parent1', true, false)
    }


}
