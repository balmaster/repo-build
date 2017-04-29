package repo.build.command

import repo.build.GitFeature
import repo.build.RepoBuild
import spock.lang.Specification

class ExportBundlesSpecification extends Specification {
    def dir = new File(".").getAbsoluteFile()

    def setup() {
        GroovyMock(GitFeature, global: true)
    }

    def "without args"() {
        def repoBuild = new RepoBuild('export-bundles')

        when:
        repoBuild.execute()

        then:
        1 * GitFeature.createManifestBundles(_, new File(dir, 'bundles'))

    }

    def "with featureBranch"() {
        def repoBuild = new RepoBuild('export-bundles', '-f', 'feature/1')

        when:
        repoBuild.execute()

        then:
        1 * GitFeature.createFeatureBundles(_, new File(dir, 'bundles'), 'feature/1')
    }

    def "with parallel with featureBranch"() {
        def repoBuild = new RepoBuild('export-bundles', '-j', '2', '-f', 'feature/1')

        when:
        repoBuild.execute()

        then:
        1 * GitFeature.createFeatureBundles(_, new File(dir, 'bundles'), 'feature/1')
    }

}
