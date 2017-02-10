package repo.build.command

import groovy.transform.CompileStatic
import repo.build.CliOptions
import repo.build.GitFeature
import repo.build.RepoEnv

class StatusCommand extends AbstractCommand {
    StatusCommand() {
        super('status', 'Get status of components')
    }

    void execute(RepoEnv env, CliOptions options) {
        GitFeature.status(env)
    }
}
