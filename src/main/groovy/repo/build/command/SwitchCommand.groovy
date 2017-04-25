package repo.build.command

import groovy.transform.CompileStatic
import repo.build.ActionContext
import repo.build.DefaultParallelActionHandler
import repo.build.GitFeature
import repo.build.CliOptions
import repo.build.RepoEnv

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
