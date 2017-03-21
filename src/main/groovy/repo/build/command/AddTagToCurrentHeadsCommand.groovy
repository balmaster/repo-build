package repo.build.command

import repo.build.CliOptions
import repo.build.GitFeature
import repo.build.RepoEnv

class AddTagToCurrentHeadsCommand extends AbstractCommand {
    AddTagToCurrentHeadsCommand() {
        super('add-tag-to-current-heads', 'Add tag to current heads')
    }

    void execute(RepoEnv env, CliOptions options) {
        def tag = options.getTag()
        GitFeature.addTagToCurrentHeads(env, tag)
    }
}
