package repo.build.command

import groovy.transform.CompileStatic
import repo.build.CliOptions
import repo.build.GitFeature
import repo.build.RepoEnv

@CompileStatic
class SyncCommand extends AbstractCommand {
    SyncCommand() {
        super('sync', 'Sync release branches for components')
    }

    void execute(RepoEnv env, CliOptions options) {
        GitFeature.sync(env)
    }
}
