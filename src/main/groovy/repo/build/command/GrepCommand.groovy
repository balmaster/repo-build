package repo.build.command

import repo.build.*

class GrepCommand extends AbstractCommand {
    GrepCommand() {
        super('grep', 'Grep expression for each components')
    }

    public static final String ACTION_EXECUTE = 'grepCommandExecute'

    void execute(RepoEnv env, CliOptions options) {
        def context = new ActionContext(env, ACTION_EXECUTE, options, new DefaultParallelActionHandler())
        context.withCloseable {
            GitFeature.grep(context, options.getExpression())
        }
    }
}
