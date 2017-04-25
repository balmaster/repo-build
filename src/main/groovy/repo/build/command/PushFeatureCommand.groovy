package repo.build.command

import groovy.transform.CompileStatic
import repo.build.ActionContext
import repo.build.DefaultParallelActionHandler
import repo.build.GitFeature
import repo.build.CliOptions
import repo.build.RepoEnv

class PushFeatureCommand extends AbstractCommand {
    PushFeatureCommand() {
        super('push-feature', 'Push feature branches ')
    }

    public static final String ACTION_EXECUTE = 'pushFeatureCommandExecute'

    void execute(RepoEnv env, CliOptions options) {
        def context = new ActionContext(env, ACTION_EXECUTE, options.getParallel(), new DefaultParallelActionHandler())
        context.withCloseable {
            GitFeature.pushFeatureBranch(context,
                    options.getFeatureBranch(), true)
        }
    }
}
