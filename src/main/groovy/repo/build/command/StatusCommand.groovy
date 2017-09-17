package repo.build.command

import repo.build.*
import repo.build.filter.OutputFilter
import repo.build.filter.UnpushedStatusFilter

class StatusCommand extends AbstractCommand {
    StatusCommand() {
        super('status', 'Get status of components')
    }

    final String ACTION_EXECUTE = 'statusCommandExecute'

    void execute(RepoEnv env, CliOptions options) {
        def context = new ActionContext(env, ACTION_EXECUTE, options, new DefaultParallelActionHandler())
        context.withCloseable {
            if (!options.showAllStatus()) {
                ArrayList<OutputFilter> filters = new ArrayList<>()
                filters.add(new UnpushedStatusFilter())
                context.outputFilter.put(ACTION_EXECUTE, filters)
            }
            return GitFeature.status(context)
        }
    }
}
