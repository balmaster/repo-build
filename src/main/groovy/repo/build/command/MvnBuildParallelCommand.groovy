package repo.build.command

import repo.build.*

class MvnBuildParallelCommand extends AbstractCommand {
    MvnBuildParallelCommand() {
        super('mvn-build-parallel', 'Execute mvn clean install for topology sorted compoents of project in parallel')
    }

    public static final String ACTION_EXECUTE = 'mvnBuildParallelExecute'

    void execute(RepoEnv env, CliOptions options) {
        def context = new ActionContext(env, ACTION_EXECUTE, options, new DefaultActionHandler())
        context.withCloseable {
            MavenFeature.buildParallel(context)
        }
    }
}
