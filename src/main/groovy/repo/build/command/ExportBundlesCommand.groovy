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
            GitFeature.createFeatureBundles(env, targetExportDir, options.getFeatureBranch())
        } else if (options.m) {
            GitFeature.createManifestBundles(env, targetExportDir)
        } else {
            throw new RepoBuildException("Use: 'repo-build -m export-bundles' or 'repo-build -f <featureBranch> export-bundles'")
        }

    }
}
