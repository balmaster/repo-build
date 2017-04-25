package repo.build.command

import groovy.transform.CompileStatic
import repo.build.ActionContext
import repo.build.DefaultParallelActionHandler
import repo.build.MavenFeature
import repo.build.CliOptions
import repo.build.RepoEnv

class ReleaseUpdateVersionsCommand extends AbstractCommand {
    ReleaseUpdateVersionsCommand() {
        super('release-update-versions', 'Update depeendencies for current release')
    }

    public static final String ACTION_EXECUTE = 'releaseUpdateVersionsCommandExecute'

    void execute(RepoEnv env, CliOptions options) {
        def context = new ActionContext(env, ACTION_EXECUTE, options.getParallel(), new DefaultParallelActionHandler())
        context.withCloseable {
            def includes = options.getIncludes()
            def continueFromComponent = options.getContinueFromComponent()
            MavenFeature.updateVersions(context,
                    options.getFeatureBranch(), includes, continueFromComponent, false)
        }
    }
}
