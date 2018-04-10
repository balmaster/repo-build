package repo.build.command

import repo.build.*

import java.nio.file.Files
import java.nio.file.Paths

class ExportBundlesCommand extends AbstractCommand {
    ExportBundlesCommand() {
        super('export-bundles', '')
    }

    public static final String ACTION_EXECUTE = 'exportBundlesCommandExecute'

    void execute(RepoEnv env, CliOptions options) {
        def context = new ActionContext(env, ACTION_EXECUTE, options, new DefaultActionHandler())
        def targetExportDir = options.getTargetExportDir()
        targetExportDir.mkdirs()
        Map<String, String> commits = null
        if (options.getCurCommitsFile()){
            commits = new HashMap<>()
            //make incremental (delta) bundles
            def commitFile = options.getCurCommitsFile()
            commitFile.eachLine { line ->
                def (key, val) = line.split(':')
                commits.put(key, val)
            }
        }
        context.withCloseable {
            if (options.hasFeatureBransh()) {
                GitFeature.createFeatureBundles(context, targetExportDir, options.getFeatureBranch(), commits)
            } else {
                GitFeature.createManifestBundles(context, targetExportDir, commits)
            }
        }

        //copy manifest file
        def basedir = context.env.getBasedir()
        Files.copy(Paths.get(basedir.getAbsolutePath(), 'manifest', 'default.xml'),
            Paths.get(targetExportDir.getAbsolutePath(), 'default.xml'))

        //make zip archive of bundles
        if (options.getZipFlag()){
            def ant = new AntBuilder()
            ant.zip( 'baseDir': targetExportDir, 'destFile': new File(targetExportDir, 'bundles.zip'))
        }
    }
}
