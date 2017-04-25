package repo.build.command

import groovy.transform.CompileStatic
import repo.build.ActionContext
import repo.build.CliOptions
import repo.build.DefaultParallelActionHandler
import repo.build.GitFeature
import repo.build.RepoEnv

class MergeAbortCommand extends AbstractCommand {
    MergeAbortCommand() {
        super('merge-abort', 'Abort merge for each component')
    }

    public static final String ACTION_EXECUTE = 'mergeAbortCommandExecute'

    void execute(RepoEnv env, CliOptions options) {
        def context = new ActionContext(env, ACTION_EXECUTE, options.getParallel(), new DefaultParallelActionHandler())
        context.withCloseable {
            GitFeature.mergeAbort(context)
        }
    }
}
