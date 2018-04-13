package repo.build.command

import com.google.common.io.Files
import groovy.xml.MarkupBuilder
import repo.build.GitFeature
import repo.build.RepoBuild
import spock.lang.Specification

class ImportBundleSpecification extends Specification {
    def dir = File.createTempFile("repo-build-test", "import_bundle")
    def tmpDir
    def setup() {
        GroovyMock(GitFeature, global: true)
        tmpDir = Files.createTempDir()
        new File(tmpDir, 'manifest.bundle').createNewFile()
        new File(tmpDir, 'b1.git').createNewFile()
    }

    def "from bundle dir with manifest in dir"() {
        def repoBuild = new RepoBuild('import-bundles', '-s', tmpDir.getAbsolutePath(), '-b', 'master')

        when:
        repoBuild.execute()

        then:

        1 * GitFeature.cloneOrUpdateFromBundle(_, tmpDir, 'manifest', 'manifest.bundle', 'master')

        1 * GitFeature.cloneOrUpdateFromBundles(_, tmpDir)

    }

    def "from bundle zip with manifest in zip"() {
        def zipFile = File.createTempFile("repo-build-test", ".zip")
        def zipFileName = zipFile.getAbsolutePath()
        zipFile.delete()

        def ant = new AntBuilder()
        ant.zip( baseDir: tmpDir, destFile: zipFileName )

        def repoBuild = new RepoBuild('import-bundles', '-s', zipFileName, '-b', 'master')

        when:
        repoBuild.execute()

        then:

        1 * GitFeature.cloneOrUpdateFromBundle(_, _, 'manifest', 'manifest.bundle', 'master')

        1 * GitFeature.cloneOrUpdateFromBundles(_, _)


    }

    def "last commit list" () {
        def tmpDir = File.createTempFile("repo-build", ".ccf")

        def repoBuild = new RepoBuild('import-bundles', '-ccf', tmpDir.absolutePath)

        when:
        repoBuild.execute()

        then:

        1 * GitFeature.lastCommitByManifest(_)
    }
}
