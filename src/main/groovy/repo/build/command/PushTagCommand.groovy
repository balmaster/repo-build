package repo.build.command

import repo.build.CliOptions
import repo.build.GitFeature
import repo.build.RepoEnv

class PushTagCommand extends AbstractCommand {
    PushTagCommand() {
        super('push-tag', 'Push tag to remote')
    }

    void execute(RepoEnv env, CliOptions options) {
        def tag = options.getTag()
        GitFeature.pushTag(env, tag)
    }
}
