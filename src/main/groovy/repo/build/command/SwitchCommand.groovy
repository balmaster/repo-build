package repo.build.command

import groovy.transform.CompileStatic
import repo.build.GitFeature
import repo.build.CliOptions
import repo.build.RepoEnv

class SwitchCommand extends AbstractCommand {
    SwitchCommand() {
        super('switch', 'Switch to feature branches and update it')
    }

    void execute(RepoEnv env, CliOptions options) {
        def featureBranch = options.getFeatureBranch()
        GitFeature.switch(env, options.getParallel(), featureBranch)
    }
}
