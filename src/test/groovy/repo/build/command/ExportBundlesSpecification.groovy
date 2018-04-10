package repo.build.command

import com.google.common.io.Files
import groovy.xml.MarkupBuilder
import repo.build.GitFeature
import repo.build.RepoBuild
import spock.lang.Specification

class ExportBundlesSpecification extends Specification {
    def dir = Files.createTempDir().getAbsoluteFile()

    def setup() {
        GroovyMock(GitFeature, global: true)
        def manifestDir = new File('.', 'manifest')
        manifestDir.mkdirs()
        new FileWriter(new File(manifestDir, 'default.xml')).withCloseable { xmlWriter ->
            def xmlMarkup = new MarkupBuilder(xmlWriter)

            xmlMarkup.'manifest'() {
                'remote'('name': 'origin', 'fetch': dir.getParentFile().getAbsolutePath())
                'default'('revision': 'refs/heads/develop', 'remote': 'origin', 'sync': '1')
                'project'('name': 'b1', 'remote': 'origin', 'path': 'b1', 'revision': 'refs/heads/master')
            }
        }
    }

    def cleanup(){
        def manifest_dir = new File('.', 'manifest')
        if (manifest_dir.exists()){
            manifest_dir.eachFile { file -> file.delete() }
            manifest_dir.delete()
        }
    }

    def "without args"() {
        def repoBuild = new RepoBuild('export-bundles', '-t', dir.absolutePath)

        when:
        repoBuild.execute()

        then:
        1 * GitFeature.createManifestBundles(_, dir, null)
        java.nio.file.Paths.get(dir.getAbsolutePath(), 'default.xml').toFile().exists()

    }

    def "with featureBranch"() {
        def repoBuild = new RepoBuild('export-bundles', '-t', dir.absolutePath, '-f', 'feature/1')

        when:
        repoBuild.execute()

        then:
        1 * GitFeature.createFeatureBundles(_, dir, 'feature/1', null)
        java.nio.file.Paths.get(dir.getAbsolutePath(), 'default.xml').toFile().exists()
    }

    def "with parallel with featureBranch"() {
        def repoBuild = new RepoBuild('export-bundles', '-t', dir.absolutePath, '-j', '2', '-f', 'feature/1')

        when:
        repoBuild.execute()

        then:
        1 * GitFeature.createFeatureBundles(_, dir, 'feature/1', null)
        java.nio.file.Paths.get(dir.getAbsolutePath(),  'default.xml').toFile().exists()
    }

    def "with zip flag"() {
        def repoBuild = new RepoBuild('export-bundles', '-t', dir.absolutePath, '-j', '2', '-z', '-f', 'feature/1')

        when:
        repoBuild.execute()

        then:
        1 * GitFeature.createFeatureBundles(_, dir, 'feature/1', null)
        java.nio.file.Paths.get(dir.getAbsolutePath(), 'default.xml').toFile().exists()
        java.nio.file.Paths.get(dir.getAbsolutePath(), 'bundles.zip').toFile().exists()
    }

    def "with ccf file"() {
        def ccfFile = File.createTempFile("repo-build", ".ccf")
        ccfFile.withWriter { outWriter ->
            [c1 : 'ae2b5a9df60786b7208d0e0488f2dd3b78902cc9', c2: '80a6dea7bd9cf3dfbc3d26055a02fe9f3f941f44']
            .each { key, value -> outWriter.write(key + ':' + value + '\n') }
        }
        def repoBuild = new RepoBuild('export-bundles', '-t', dir.absolutePath, '-j', '2', '-z', '-f',
                'feature/1', '-ccf', ccfFile.getAbsolutePath())

        when:
        repoBuild.execute()

        then:
        1 * GitFeature.createFeatureBundles(_, dir, 'feature/1',
                [c1 : 'ae2b5a9df60786b7208d0e0488f2dd3b78902cc9', c2: '80a6dea7bd9cf3dfbc3d26055a02fe9f3f941f44'])
        java.nio.file.Paths.get(dir.getAbsolutePath(), 'default.xml').toFile().exists()

    }

}
