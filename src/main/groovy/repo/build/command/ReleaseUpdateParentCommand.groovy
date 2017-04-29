package repo.build.command

import repo.build.*

class ReleaseUpdateParentCommand extends AbstractCommand {
    ReleaseUpdateParentCommand() {
        super('release-update-parent', 'Update parent for each component')
    }

    public static final String ACTION_EXECUTE = 'releaseUpdateParentCommandExecute'

    void execute(RepoEnv env, CliOptions options) {
        def context = new ActionContext(env, ACTION_EXECUTE, options.getParallel(), new DefaultParallelActionHandler())
        context.withCloseable {
            def parentComponent = options.getParent()
            MavenFeature.updateParent(context,
                    options.getFeatureBranch(), parentComponent, true, false)
        }
    }
}
