package repo.build.command

import repo.build.*

class MergeAbortCommand extends AbstractCommand {
    MergeAbortCommand() {
        super('merge-abort', 'Abort merge for each component')
    }

    public static final String ACTION_EXECUTE = 'mergeAbortCommandExecute'

    void execute(RepoEnv env, CliOptions options) {
        def context = new ActionContext(env, ACTION_EXECUTE, options, new DefaultParallelActionHandler())
        context.withCloseable {
            GitFeature.mergeAbort(context)
        }
    }
}
