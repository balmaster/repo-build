package repo.build.command

import repo.build.*

class MvnBuildCommand extends AbstractCommand {
    MvnBuildCommand() {
        super('mvn-build', 'Execute mvn clean install for parent component, then execute mvn clean install for whole project ')
    }

    public static final String ACTION_EXECUTE = 'mvnBuildExecute'

    void execute(RepoEnv env, CliOptions options) {
        def context = new ActionContext(env, ACTION_EXECUTE, options.getParallel(), new DefaultParallelActionHandler())
        context.withCloseable {
            // get parent component
            def parentComponent = options.getParent()
            def parentPom = new File(env.basedir, "$parentComponent/pom.xml")
            def systemProperties = options.getSystemProperties()
            // build parent
            MavenFeature.build(context, parentPom, ['clean', 'install'], systemProperties)
            // build project
            MavenFeature.build(context, new File(env.basedir, 'pom.xml'), ['clean', 'install'], systemProperties)
        }
    }
}
