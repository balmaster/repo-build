package repo.build.command

import repo.build.*

class SwitchCommand extends AbstractCommand {
    SwitchCommand() {
        super('switch', 'Switch to feature and optionally task branches and update its')
    }

    public static final String ACTION_EXECUTE = 'switchCommandExecute'

    void execute(RepoEnv env, CliOptions options) {
        def context = new ActionContext(env, ACTION_EXECUTE, options, new DefaultActionHandler())
        context.withCloseable {
            GitFeature.switch(context,
                    options.getFeatureBranch(),
                    options.getTaskBranch()
            )
        }
    }
}
