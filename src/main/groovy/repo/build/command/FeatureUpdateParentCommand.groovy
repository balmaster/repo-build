package repo.build.command

import repo.build.*

class FeatureUpdateParentCommand extends AbstractCommand {
    FeatureUpdateParentCommand() {
        super('feature-update-parent', 'Update parent for each componnent with feature branch')
    }

    public static final String ACTION_EXECUTE = 'featureUpdateParentCommandExecute'

    void execute(RepoEnv env, CliOptions options) {
        def context = new ActionContext(env, ACTION_EXECUTE, options.getParallel(), new DefaultParallelActionHandler())
        context.withCloseable {
            def parentComponent = options.getParent()
            MavenFeature.updateFeatureParent(context,
                    options.getFeatureBranch(),
                    parentComponent,
                    false,
                    true,
                    options.getSystemProperties())
        }
    }
}
