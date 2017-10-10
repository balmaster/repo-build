package repo.build.command

import repo.build.*

class BuildPomCommand extends AbstractCommand {
    public static final String ACTION_EXECUTE = 'buildPomCommandExecute'

    BuildPomCommand() {
        super('execute-pom', 'Generate main pom.xml for all components')
    }

    void execute(RepoEnv env, CliOptions options) {
        def context = new ActionContext(env, ACTION_EXECUTE, options, new DefaultActionHandler())
        context.withCloseable {
            def pomFile = options.getPomFile()
            def dirName = pomFile.parentFile.name
            if (dirName == '.') {
                dirName = pomFile.parentFile.parentFile.name
            }

            def nameSuffix = options.hasFeatureBransh() ?
                    options.getFeatureBranch() :
                    Git.getBranch(context, new File(options.getRepoBasedir(), "manifest"))
            Pom.generateXml(context, "$dirName-$nameSuffix", pomFile)
        }
    }
}
