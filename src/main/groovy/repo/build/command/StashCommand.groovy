package repo.build.command

import groovy.transform.CompileStatic
import repo.build.ActionContext
import repo.build.CliOptions
import repo.build.DefaultParallelActionHandler
import repo.build.GitFeature
import repo.build.RepoEnv

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
