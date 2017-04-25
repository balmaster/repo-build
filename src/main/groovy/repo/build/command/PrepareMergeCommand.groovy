package repo.build.command

import groovy.transform.CompileStatic
import repo.build.ActionContext
import repo.build.DefaultParallelActionHandler
import repo.build.GitFeature
import repo.build.CliOptions
import repo.build.RepoEnv

class PrepareMergeCommand extends AbstractCommand {

    PrepareMergeCommand() {
        super('prepare-merge', 'Check possibility of auto merge feature branches into release')
    }

    public static final String ACTION_EXECUTE = 'prepareMergeCommandExecute'

    void execute(RepoEnv env, CliOptions options) {
        def context = new ActionContext(env, ACTION_EXECUTE, options.getParallel(), new DefaultParallelActionHandler())
        context.withCloseable {
            GitFeature.mergeFeature(context,
                    options.getFeatureBranch(),
                    options.getAllFlag())
        }
    }
}
