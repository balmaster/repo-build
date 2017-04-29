package repo.build.command

import repo.build.*

class SyncCommand extends AbstractCommand {
    SyncCommand() {
        super('sync', 'Sync release branches for components')
    }

    public static final String ACTION_EXECUTE = 'sysCommandExecute'

    void execute(RepoEnv env, CliOptions options) {
        def context = new ActionContext(env, ACTION_EXECUTE, options.getParallel(), new DefaultParallelActionHandler())
        context.withCloseable {
            GitFeature.sync(context)
        }
    }
}
