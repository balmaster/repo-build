package repo.build.command

import groovy.transform.CompileStatic
import repo.build.CliOptions
import repo.build.GitFeature
import repo.build.RepoEnv

class MergeAbortCommand extends AbstractCommand {
    MergeAbortCommand() {
        super('merge-abort', 'Abort merge for each component')
    }

    void execute(RepoEnv env, CliOptions options) {
        GitFeature.mergeAbort(env)
    }
}
