package repo.build.command

import repo.build.*

class StashPopCommand extends AbstractCommand {
    StashPopCommand() {
        super('stash-pop', 'Restore changes for each component')
    }

    public static final String ACTION_EXECUTE = 'stashPopCommandExecute'

    void execute(RepoEnv env, CliOptions options) {
        def context = new ActionContext(env,ACTION_EXECUTE,options.getParallel(),new DefaultParallelActionHandler())
        context.withCloseable {
            GitFeature.stashPop(context)
        }
    }
}
