package repo.build.command

import repo.build.GitFeature
import repo.build.RepoBuild
import repo.build.RepoBuildException
import spock.lang.Specification

class InitSpecification extends Specification {

    def setup() {
        GroovyMock(GitFeature, global: true)
    }

    def "without args"() {
        def repoBuild = new RepoBuild('grep')

        when:
        repoBuild.execute()

        then:
        thrown(RepoBuildException)

    }

    def "clean with manifestUrl"() {
        def repoBuild = new RepoBuild('init', '-M', 'manifestUrl')

        when:
        repoBuild.execute()

        then:
        thrown(RepoBuildException)
    }

    def "clean with manifestUrl and manifestBranch"() {
        def repoBuild = new RepoBuild('init', '-M', 'manifestUrl', '-b', 'manifestBranch')

        when:
        repoBuild.execute()

        then:
        1 * GitFeature.cloneManifest(_, 'manifestUrl', 'manifestBranch')
    }

    def "exists with manifestBranch"() {
        def repoBuild = new RepoBuild('init', '-b', 'manifestBranch')
        repoBuild.env.manifest = new Node(null, 'project')

        when:
        repoBuild.execute()

        then:
        1 * GitFeature.updateManifest(_, 'manifestBranch')
    }

}
