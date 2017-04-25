package repo.build.command

import repo.build.ActionContext
import repo.build.DefaultParallelActionHandler
import repo.build.GitFeature
import repo.build.CliOptions
import repo.build.RepoBuildException
import repo.build.RepoEnv

class ExportBundlesCommand extends AbstractCommand {
    ExportBundlesCommand() {
        super('export-bundles', '')
    }

    public static final String ACTION_EXECUTE = 'exportBundlesCommandExecute'

    void execute(RepoEnv env, CliOptions options) {
        def context = new ActionContext(env, ACTION_EXECUTE, options.getParallel(), new DefaultParallelActionHandler())
        def targetExportDir = options.getTargetExportDir()
        targetExportDir.mkdirs()
        context.withCloseable {
            if (options.hasFeatureBransh()) {
                GitFeature.createFeatureBundles(context, targetExportDir, options.getFeatureBranch())
            } else {
                GitFeature.createManifestBundles(context, targetExportDir)
            }
        }
    }
}
