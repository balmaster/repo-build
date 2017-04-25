package repo.build.command

import repo.build.ActionContext
import repo.build.CliOptions
import repo.build.DefaultParallelActionHandler
import repo.build.GitFeature
import repo.build.RepoEnv

class PushTagCommand extends AbstractCommand {
    PushTagCommand() {
        super('push-tag', 'Push tag to remote')
    }

    public static final String ACTION_EXECUTE = 'pushTagCommandExecute'

    void execute(RepoEnv env, CliOptions options) {
        def context = new ActionContext(env, ACTION_EXECUTE, options.getParallel(), new DefaultParallelActionHandler())
        context.withCloseable {
            def tag = options.getTag()
            GitFeature.pushTag(context, tag)
        }
    }
}
