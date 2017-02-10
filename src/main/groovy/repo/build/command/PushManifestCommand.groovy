package repo.build.command

import groovy.transform.CompileStatic
import repo.build.CliOptions
import repo.build.GitFeature
import repo.build.RepoEnv

class PushManifestCommand extends AbstractCommand {
    PushManifestCommand() {
        super('push-manifest', 'Push current manifest branches')
    }

    void execute(RepoEnv env, CliOptions options) {
        GitFeature.pushManifestBranch(env, true)
    }
}
