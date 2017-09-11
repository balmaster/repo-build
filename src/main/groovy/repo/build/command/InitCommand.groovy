package repo.build.command

import repo.build.*

class InitCommand extends AbstractCommand {
    InitCommand() {
        super('init', 'Initialize project and set manifest branch')
    }

    public static final String ACTION_EXECUTE = 'initCommandExecute'

    void execute(RepoEnv env, CliOptions options) {
        def context = new ActionContext(env, ACTION_EXECUTE, options, new DefaultParallelActionHandler())
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
