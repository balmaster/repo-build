package repo.build.command

import repo.build.*

class MvnBuildCommand extends AbstractCommand {
    MvnBuildCommand() {
        super('mvn-execute', 'Execute mvn clean install for parent component, then execute mvn clean install for whole project ')
    }

    public static final String ACTION_EXECUTE = 'mvnBuildExecute'

    void execute(RepoEnv env, CliOptions options) {
        def context = new ActionContext(env, ACTION_EXECUTE, options, new DefaultActionHandler())
        context.withCloseable {
            // get parent component
            def systemProperties = options.getSystemProperties()
            // build parents
            MavenFeature.buildParents(context)
            // build root generated pom
            Maven.execute(context, new File(env.basedir, 'pom.xml'), ['clean', 'install'], systemProperties)
        }
    }
}
