package repo.build.command

import repo.build.*

class TaskMergeFeatureCommand extends AbstractCommand {
    TaskMergeFeatureCommand() {
        super('task-merge-feature', 'Merge feature branches into current task')
    }

    public static final String ACTION_EXECUTE = 'taskMergeFeatureCommandExecute'

    void execute(RepoEnv env, CliOptions options) {
        def context = new ActionContext(env, ACTION_EXECUTE, options, new DefaultParallelActionHandler())
        context.withCloseable {
            GitFeature.taskMergeFeature(context,
                    options.getRequiredTaskBranch(),
                    options.getFeatureBranch())
        }
    }
}
