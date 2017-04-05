package repo.build.command

import repo.build.GitFeature
import repo.build.CliOptions
import repo.build.RepoBuildException
import repo.build.RepoEnv

class ExportBundlesCommand extends AbstractCommand {
    ExportBundlesCommand() {
        super('export-bundles', '')
    }

    void execute(RepoEnv env, CliOptions options) {
        def targetExportDir = options.getTargetExportDir()
        targetExportDir.mkdirs()
        if (options.hasFeatureBransh()) {
            GitFeature.createFeatureBundles(env, options.getParallel(), targetExportDir, options.getFeatureBranch())
        } else {
            GitFeature.createManifestBundles(env, options.getParallel(), targetExportDir)
        }
    }
}
