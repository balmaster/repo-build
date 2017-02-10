package repo.build.command

import groovy.transform.CompileStatic
import repo.build.CliOptions
import repo.build.GitFeature
import repo.build.RepoEnv

class StashPopCommand extends AbstractCommand {
    StashPopCommand() {
        super('stash-pop', 'Restore changes for each component')
    }

    void execute(RepoEnv env, CliOptions options) {
        GitFeature.stashPop(env)
    }
}
