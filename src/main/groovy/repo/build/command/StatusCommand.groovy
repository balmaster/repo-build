package repo.build.command

import repo.build.*

class StatusCommand extends AbstractCommand {
    StatusCommand() {
        super('status', 'Get status of components')
    }

    public stati
    final String ACTION_EXECUTE = 'statusCommandExecute'

    void execute(RepoEnv env, CliOptions options) {
        def context = new ActionContext(env, ACTION_EXECUTE, options.getParallel(), new DefaultParallelActionHandler())
        context.withCloseable {
            GitFeature.status(context)
        }
    }
}
