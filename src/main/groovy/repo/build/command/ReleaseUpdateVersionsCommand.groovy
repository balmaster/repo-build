package repo.build.command

import repo.build.*

class ReleaseUpdateVersionsCommand extends AbstractCommand {
    ReleaseUpdateVersionsCommand() {
        super('release-update-versions', 'Update dependencies for current release')
    }

    public static final String ACTION_EXECUTE = 'releaseUpdateVersionsCommandExecute'

    void execute(RepoEnv env, CliOptions options) {
        def context = new ActionContext(env, ACTION_EXECUTE, options, new DefaultActionHandler())
        context.withCloseable {
            def includes = options.getIncludes()
            def continueFromComponent = options.getContinueFromComponent()
            MavenFeature.releaseUpdateVersions(context,
                    includes,
                    continueFromComponent)
        }
    }
}
