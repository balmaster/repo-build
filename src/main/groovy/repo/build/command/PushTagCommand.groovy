package repo.build.command

import repo.build.*

class PushTagCommand extends AbstractCommand {
    PushTagCommand() {
        super('push-tag', 'Push tag to remote')
    }

    public static final String ACTION_EXECUTE = 'pushTagCommandExecute'

    void execute(RepoEnv env, CliOptions options) {
        def context = new ActionContext(env, ACTION_EXECUTE, options, new DefaultParallelActionHandler())
        context.withCloseable {
            def tag = options.getTag()
            GitFeature.pushTag(context, tag)
        }
    }
}
