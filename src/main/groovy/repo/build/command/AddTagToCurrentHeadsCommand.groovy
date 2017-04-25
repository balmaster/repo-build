package repo.build.command

import repo.build.ActionContext
import repo.build.CliOptions
import repo.build.DefaultParallelActionHandler
import repo.build.GitFeature
import repo.build.RepoEnv

class AddTagToCurrentHeadsCommand extends AbstractCommand {
    AddTagToCurrentHeadsCommand() {
        super('add-tag-to-current-heads', 'Add tag to current heads')
    }

    public static final String ACTION_EXECUTE = 'addTagToCurrentHeadsExecute'

    void execute(RepoEnv env, CliOptions options) {
        def context = new ActionContext(env,ACTION_EXECUTE, options.getParallel(),new DefaultParallelActionHandler())
        def tag = options.getTag()
        context.withCloseable {
            GitFeature.addTagToCurrentHeads(context, tag)
        }
    }
}
