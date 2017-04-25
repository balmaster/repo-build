package repo.build.command

import groovy.transform.CompileStatic
import repo.build.ActionContext
import repo.build.CliOptions
import repo.build.DefaultParallelActionHandler
import repo.build.GitFeature
import repo.build.RepoEnv

class StashPopCommand extends AbstractCommand {
    StashPopCommand() {
        super('stash-pop', 'Restore changes for each component')
    }

    public static final String ACTION_EXECUTE = 'stashPopCommandExecute'

    void execute(RepoEnv env, CliOptions options) {
        def context = new ActionContext(env,ACTION_EXECUTE,options.getParallel(),new DefaultParallelActionHandler())
        context.withCloseable {
            GitFeature.stashPop(context)
        }
    }
}
