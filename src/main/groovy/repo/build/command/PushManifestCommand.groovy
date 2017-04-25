package repo.build.command

import groovy.transform.CompileStatic
import repo.build.ActionContext
import repo.build.CliOptions
import repo.build.DefaultParallelActionHandler
import repo.build.GitFeature
import repo.build.RepoEnv

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
