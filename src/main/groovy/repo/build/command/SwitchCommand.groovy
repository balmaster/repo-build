package repo.build.command

import repo.build.*

class SwitchCommand extends AbstractCommand {
    SwitchCommand() {
        super('switch', 'Switch to feature branches and update it')
    }

    public static final String ACTION_EXECUTE = 'switchCommandExecute'

    void execute(RepoEnv env, CliOptions options) {
        def context = new ActionContext(env, ACTION_EXECUTE, options.getParallel(), new DefaultParallelActionHandler())
        context.withCloseable {
            def featureBranch = options.getFeatureBranch()
            GitFeature.switch(context, featureBranch)
        }
    }
}
