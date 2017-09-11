package repo.build.command

import repo.build.*

class ExportBundlesCommand extends AbstractCommand {
    ExportBundlesCommand() {
        super('export-bundles', '')
    }

    public static final String ACTION_EXECUTE = 'exportBundlesCommandExecute'

    void execute(RepoEnv env, CliOptions options) {
        def context = new ActionContext(env, ACTION_EXECUTE, options, new DefaultParallelActionHandler())
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
