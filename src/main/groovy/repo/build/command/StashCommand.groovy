package repo.build.command

import groovy.transform.CompileStatic
import repo.build.CliOptions
import repo.build.GitFeature
import repo.build.RepoEnv

class StashCommand extends AbstractCommand {
    StashCommand() {
        super('stash', 'Save changes for each component')
    }

    void execute(RepoEnv env, CliOptions options) {
        GitFeature.stash(env)
    }
}
