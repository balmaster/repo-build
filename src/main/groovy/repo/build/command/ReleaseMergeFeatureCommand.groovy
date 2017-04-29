package repo.build.command

import repo.build.*

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
