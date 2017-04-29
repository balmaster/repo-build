package repo.build.command

import repo.build.*

class StashCommand extends AbstractCommand {
    StashCommand() {
        super('stash', 'Save changes for each component')
    }

    public static final String ACTION_EXECUTE = 'stashCommandExecute'

    void execute(RepoEnv env, CliOptions options) {
        def context = new ActionContext(env, ACTION_EXECUTE, options.getParallel(), new DefaultParallelActionHandler())
        context.withCloseable {
            GitFeature.stash(context)
        }
    }
}
