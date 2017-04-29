package repo.build.command

import repo.build.*

class PushManifestCommand extends AbstractCommand {
    PushManifestCommand() {
        super('push-manifest', 'Push current manifest branches')
    }

    public static final String ACTION_EXECUTE = 'pushManifestCommandExecute'

    void execute(RepoEnv env, CliOptions options) {
        def context = new ActionContext(env, ACTION_EXECUTE, options.getParallel(), new DefaultParallelActionHandler())
        context.withCloseable {
            GitFeature.pushManifestBranch(context, true)
        }
    }
}
