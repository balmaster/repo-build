package repo.build.command

import repo.build.*

class PushFeatureCommand extends AbstractCommand {
    PushFeatureCommand() {
        super('push-feature', 'Push feature branches ')
    }

    public static final String ACTION_EXECUTE = 'pushFeatureCommandExecute'

    void execute(RepoEnv env, CliOptions options) {
        def context = new ActionContext(env, ACTION_EXECUTE, options, new DefaultParallelActionHandler())
        context.withCloseable {
            GitFeature.pushFeatureBranch(context,
                    options.getFeatureBranch(), true)
        }
    }
}
