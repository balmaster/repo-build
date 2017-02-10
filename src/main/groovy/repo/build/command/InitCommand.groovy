package repo.build.command

import groovy.transform.CompileStatic
import repo.build.GitFeature
import repo.build.CliOptions
import repo.build.RepoBuildException
import repo.build.RepoEnv

class InitCommand extends AbstractCommand {
    InitCommand() {
        super('init', 'Initialize project and set manifest branch')
    }

    void execute(RepoEnv env, CliOptions options) {
        def manifestBranch = options.getManifestBranch()
        if (!env.manifest) {
            def manifestUrl = options.getManifestUrl()
            if (!manifestUrl || !manifestBranch) {
                throw new RepoBuildException("Use: 'repo-build -M <manifestUrl> -b <manifestBranch>'")
            }
            GitFeature.cloneManifest(env, manifestUrl, manifestBranch)
        } else {
            GitFeature.updateManifest(env, manifestBranch)
        }
        env.openManifest()
    }
}
