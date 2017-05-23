package repo.build.command

import repo.build.*

class PrepareMergeCommand extends AbstractCommand {

    PrepareMergeCommand() {
        super('prepare-merge', 'Check possibility of auto merge feature branches into release')
    }

    public static final String ACTION_EXECUTE = 'prepareMergeCommandExecute'

    void execute(RepoEnv env, CliOptions options) {
        def context = new ActionContext(env, ACTION_EXECUTE, options.getParallel(), new DefaultParallelActionHandler())
        context.withCloseable {
            GitFeature.releaseMergeFeature(context,
                    options.getFeatureBranch(),
                    options.getAllFlag())
        }
    }
}
