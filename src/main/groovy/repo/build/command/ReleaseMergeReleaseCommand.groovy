package repo.build.command

import repo.build.ActionContext
import repo.build.CliOptions
import repo.build.DefaultActionHandler
import repo.build.GitFeature
import repo.build.RepoEnv

class ReleaseMergeReleaseCommand extends AbstractCommand {

    ReleaseMergeReleaseCommand() {
        super('release-merge-release', 'Merge one release in another release')
    }

    private static final String ACTION_EXECUTE = 'releaseMergeReleaseCommandExecute'

    void execute(RepoEnv env, CliOptions options) {
        def context = new ActionContext(env, ACTION_EXECUTE, options, new DefaultActionHandler())
        context.withCloseable {
            GitFeature.releaseMergeRelease(context, options.getOneManifestBranch(), options.getTwoManifestBranch())
        }
    }
}
