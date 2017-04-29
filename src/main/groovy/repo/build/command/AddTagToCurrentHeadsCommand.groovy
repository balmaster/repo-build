package repo.build.command

import repo.build.*

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
