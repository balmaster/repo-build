package repo.build.command

import groovy.transform.CompileStatic
import repo.build.ActionContext
import repo.build.DefaultParallelActionHandler
import repo.build.GitFeature
import repo.build.CliOptions
import repo.build.RepoBuildException
import repo.build.RepoEnv

class InitCommand extends AbstractCommand {
    InitCommand() {
        super('init', 'Initialize project and set manifest branch')
    }

    public static final String ACTION_EXECUTE = 'initCommandExecute'

    void execute(RepoEnv env, CliOptions options) {
        def context = new ActionContext(env, ACTION_EXECUTE, options.getParallel(), new DefaultParallelActionHandler())
        context.withCloseable {
            def manifestBranch = options.getManifestBranch()
            if (!env.manifest) {
                def manifestUrl = options.getManifestUrl()
                if (!manifestUrl || !manifestBranch) {
                    throw new RepoBuildException("Use: 'repo-build -M <manifestUrl> -b <manifestBranch>'")
                }
                GitFeature.cloneManifest(context, manifestUrl, manifestBranch)
            } else {
                GitFeature.updateManifest(context, manifestBranch)
            }
            env.openManifest()
        }
    }
}
