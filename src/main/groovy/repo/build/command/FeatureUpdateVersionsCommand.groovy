package repo.build.command

import groovy.transform.CompileStatic
import repo.build.ActionContext
import repo.build.DefaultParallelActionHandler
import repo.build.MavenFeature
import repo.build.CliOptions
import repo.build.RepoEnv

class FeatureUpdateVersionsCommand extends AbstractCommand {
    FeatureUpdateVersionsCommand() {
        super('feature-update-versions', 'Update dependencies for each componet with feature branch')
    }

    public static final String ACTION_EXECUTE = 'featureUpdateVersionsCommandExecute'

    void execute(RepoEnv env, CliOptions options) {
        def context = new ActionContext(env, ACTION_EXECUTE, options.getParallel(), new DefaultParallelActionHandler())
        context.withCloseable {
            def includes = options.getIncludes()
            def continueFromComponent = options.getContinueFromComponent()
            MavenFeature.updateVersions(context, options.getFeatureBranch(), includes, continueFromComponent, true)
        }
    }
}
