package repo.build.command

import groovy.transform.CompileStatic
import repo.build.GitFeature
import repo.build.CliOptions
import repo.build.RepoEnv

class PrepareMergeCommand extends AbstractCommand {

    PrepareMergeCommand() {
        super('prepare-merge', 'Check possibility of auto merge feature branches into release')
    }

    void execute(RepoEnv env, CliOptions options) {
        GitFeature.mergeFeature(env,
                options.getFeatureBranch(),
                options.getAllFlag())
    }
}
