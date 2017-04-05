package repo.build.command

import groovy.transform.CompileStatic
import repo.build.GitFeature
import repo.build.CliOptions
import repo.build.RepoEnv

class ReleaseMergeFeatureCommand extends AbstractCommand {
    ReleaseMergeFeatureCommand() {
        super('release-merge-feature', 'Merge feature branches into current release')
    }

    void execute(RepoEnv env, CliOptions options) {
        GitFeature.mergeFeature(env,
                options.getParallel(),
                options.getFeatureBranch())
    }
}
