package repo.build.command

import groovy.transform.CompileStatic
import repo.build.GitFeature
import repo.build.CliOptions
import repo.build.RepoEnv

class FeatureMergeReleaseCommand extends AbstractCommand {
    FeatureMergeReleaseCommand() {
        super('feature-merge-release', 'Merge current releease into feature branches')
    }

    void execute(RepoEnv env, CliOptions options) {
        GitFeature.mergeRelease(env, options.getParallel(), options.getFeatureBranch())
    }
}
