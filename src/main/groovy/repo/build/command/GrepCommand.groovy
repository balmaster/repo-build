package repo.build.command

import groovy.transform.CompileStatic
import repo.build.ActionContext
import repo.build.DefaultParallelActionHandler
import repo.build.GitFeature
import repo.build.CliOptions
import repo.build.RepoEnv

class GrepCommand extends AbstractCommand {
    GrepCommand() {
        super('grep', 'Grep expression for each components')
    }

    public static final String ACTION_EXECUTE = 'grepCommandExecute'

    void execute(RepoEnv env, CliOptions options) {
        def context = new ActionContext(env, ACTION_EXECUTE, options.getParallel(), new DefaultParallelActionHandler())
        context.withCloseable {
            GitFeature.grep(context, options.getExpression())
        }
    }
}
