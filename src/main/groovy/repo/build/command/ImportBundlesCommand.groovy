package repo.build.command

import groovy.xml.XmlUtil
import repo.build.*

import java.nio.file.Files

class ImportBundlesCommand extends AbstractCommand{
    ImportBundlesCommand() {
        super('import-bundles', '')
    }

    public static final String ACTION_EXECUTE = 'importBundlesCommandExecute'

    void execute(RepoEnv env, CliOptions options) {
        def context = new ActionContext(env, ACTION_EXECUTE, options, new DefaultActionHandler())

        context.withCloseable {
            //generate current commits file for creating increment (delta) bundles
            if (options.getCurCommitsFile()){
                def commitFile = options.getCurCommitsFile()
                def lastCommits = GitFeature.lastCommitByManifest(context)
                commitFile.withWriter { outWriter -> lastCommits.each { key, value -> outWriter.write(key + ':' + value + '\n') } }
                return
            }

            def sourceImportDir = options.getSourceImportDir()
            if (!sourceImportDir.isDirectory()){
                //source dir is not directory. we suggest that it's zip archive
                //extract zip archive to tmp

                def ant = new AntBuilder();
                def tmpDir = Files.createTempDirectory("repo-build-bundles")
                ant.unzip(  src: sourceImportDir,
                        dest: tmpDir,
                        overwrite:"false" )

                //rewrite source path to extracted archive folder
                sourceImportDir = tmpDir.toFile()
            }

            def manifestFile = options.getManifestFile()
            if (manifestFile == null){
                //manifest file not specified. expect to be found in source folder
                manifestFile = sourceImportDir.listFiles({ fileDir, fileName -> fileName.equals("default.xml") } as FilenameFilter)[0]
            }

            def xml = XmlUtils.transformManifestForBundle(manifestFile, sourceImportDir)

            //save rewritten manifest in the base dir
            def manifestFolder = new File(context.env.getBasedir(), 'manifest')
            manifestFolder.mkdirs()
            def convertedManifestFile = new File(manifestFolder, 'default.xml')

            convertedManifestFile.withWriter { outWriter ->
                XmlUtil.serialize( xml, outWriter )
            }

            //init git repository for manifest
            GitFeature.initManifestRepository(context, manifestFolder, 'extracted_from_bundle')

            context.env.openManifest()

            GitFeature.cloneOrUpdateFromBundle(context, sourceImportDir)
        }
    }
}
