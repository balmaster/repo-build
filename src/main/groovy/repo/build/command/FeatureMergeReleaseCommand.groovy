package repo.build.command

import groovy.transform.CompileStatic
import repo.build.ActionContext
import repo.build.DefaultParallelActionHandler
import repo.build.GitFeature
import repo.build.CliOptions
import repo.build.RepoEnv

class FeatureMergeReleaseCommand extends AbstractCommand {
    FeatureMergeReleaseCommand() {
        super('feature-merge-release', 'Merge current releease into feature branches')
    }

    public stativ
    final String ACTION_EXECUTE = 'featureMergeReleaseCommandExecute'

    void execute(RepoEnv env, CliOptions options) {
        def context = new ActionContext(env, ACTION_EXECUTE, options.getParallel(), new DefaultParallelActionHandler())
        context.withCloseable {
            GitFeature.mergeRelease(context, options.getFeatureBranch())
        }
    }
}
