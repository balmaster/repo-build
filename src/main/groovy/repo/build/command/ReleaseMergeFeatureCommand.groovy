package repo.build.command

import groovy.transform.CompileStatic
import repo.build.ActionContext
import repo.build.DefaultParallelActionHandler
import repo.build.GitFeature
import repo.build.CliOptions
import repo.build.RepoEnv

class ReleaseMergeFeatureCommand extends AbstractCommand {
    ReleaseMergeFeatureCommand() {
        super('release-merge-feature', 'Merge feature branches into current release')
    }

    public static final String ACTION_EXECUTE = 'releaseMergeFeatureCommandExecute'

    void execute(RepoEnv env, CliOptions options) {
        def context = new ActionContext(env, ACTION_EXECUTE, options.getParallel(), new DefaultParallelActionHandler())
        context.withCloseable {
            GitFeature.mergeFeature(context, options.getFeatureBranch())
        }
    }
}
