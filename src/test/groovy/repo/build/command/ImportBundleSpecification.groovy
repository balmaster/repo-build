package repo.build.command

import com.google.common.io.Files
import groovy.xml.MarkupBuilder
import repo.build.GitFeature
import repo.build.RepoBuild
import spock.lang.Specification

class ImportBundleSpecification extends Specification {
    def dir = File.createTempFile("repo-build-test", "import_bundle")
    def setup() {
        GroovyMock(GitFeature, global: true)
    }

    def cleanup(){
        def manifest_dir = new File('.', 'manifest')
        if (manifest_dir.exists()){
            manifest_dir.eachFile { file -> file.delete() }
            manifest_dir.delete()
        }
    }

    //import-bundles -mf /home/mike/projects/test/repo-build/bundles/default.xml -s /tmp/bundles.zip build-pom

    def "from bundle dir with manifest in dir"() {
        def tmpDir = Files.createTempDir()
        new File(tmpDir, 'b1.bundle').createNewFile()

        new FileWriter(new File(tmpDir, 'default.xml')).withCloseable { xmlWriter ->
            def xmlMarkup = new MarkupBuilder(xmlWriter)

            xmlMarkup.'manifest'() {
                'remote'('name': 'origin', 'fetch': dir.getParentFile().getAbsolutePath())
                'default'('revision': 'refs/heads/develop', 'remote': 'origin', 'sync': '1')
                'project'('name': 'b1', 'remote': 'origin', 'path': 'b1', 'revision': 'refs/heads/master')
            }
        }

        def repoBuild = new RepoBuild('import-bundles', '-s', tmpDir.getAbsolutePath())

        when:
        repoBuild.execute()

        then:

        1 * GitFeature.initManifestRepository(_, _, 'extracted_from_bundle')

        1 * GitFeature.cloneOrUpdateFromBundle(_, tmpDir)

    }

    def "from bundle zip with manifest in zip"() {
        def tmpDir = Files.createTempDir()
        new File(tmpDir, 'b1.bundle').createNewFile()

        new FileWriter(new File(tmpDir, 'default.xml')).withCloseable { xmlWriter ->
            def xmlMarkup = new MarkupBuilder(xmlWriter)

            xmlMarkup.'manifest'() {
                'remote'('name': 'origin', 'fetch': dir.getParentFile().getAbsolutePath())
                'default'('revision': 'refs/heads/develop', 'remote': 'origin', 'sync': '1')
                'project'('name': 'b1', 'remote': 'origin', 'path': 'b1', 'revision': 'refs/heads/master')
            }
        }

        def zipFile = File.createTempFile("repo-build-test", ".zip")
        def zipFileName = zipFile.getAbsolutePath()
        zipFile.delete()

        def ant = new AntBuilder()
        ant.zip( baseDir: tmpDir, destFile: zipFileName )

        def repoBuild = new RepoBuild('import-bundles', '-s', zipFileName)

        when:
        repoBuild.execute()

        then:

        1 * GitFeature.initManifestRepository(_, _, 'extracted_from_bundle')

        1 * GitFeature.cloneOrUpdateFromBundle(_, _)


    }

    def "from bundle dir with separate manifest" () {
        def tmpDir = Files.createTempDir()
        new File(tmpDir, 'b1.bundle').createNewFile()

        def manifestTmpDir = Files.createTempDir()

        new FileWriter(new File(manifestTmpDir, 'default.xml')).withCloseable { xmlWriter ->
            def xmlMarkup = new MarkupBuilder(xmlWriter)

            xmlMarkup.'manifest'() {
                'remote'('name': 'origin', 'fetch': dir.getParentFile().getAbsolutePath())
                'default'('revision': 'refs/heads/develop', 'remote': 'origin', 'sync': '1')
                'project'('name': 'b1', 'remote': 'origin', 'path': 'b1', 'revision': 'refs/heads/master')
            }
        }

        def repoBuild = new RepoBuild('import-bundles', '-s', tmpDir.getAbsolutePath(),
                '-mf', new File(manifestTmpDir, 'default.xml').getAbsolutePath())

        when:
        repoBuild.execute()

        then:

        1 * GitFeature.initManifestRepository(_, _, 'extracted_from_bundle')

        1 * GitFeature.cloneOrUpdateFromBundle(_, tmpDir)

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
