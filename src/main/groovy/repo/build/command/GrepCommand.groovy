package repo.build.command

import groovy.transform.CompileStatic
import repo.build.GitFeature
import repo.build.CliOptions
import repo.build.RepoEnv

@CompileStatic
class GrepCommand extends AbstractCommand {
    GrepCommand() {
        super('grep', 'Grep expression for each components')
    }

    void execute(RepoEnv env, CliOptions options) {
        def expr = options.getExpression()
        GitFeature.grep(env, expr)
    }
}
