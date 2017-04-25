package repo.build.command

import groovy.transform.CompileStatic
import repo.build.ActionContext
import repo.build.CliOptions
import repo.build.DefaultParallelActionHandler
import repo.build.GitFeature
import repo.build.RepoEnv

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
