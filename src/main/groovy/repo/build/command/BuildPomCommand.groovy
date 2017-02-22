package repo.build.command

import repo.build.Git
import repo.build.CliOptions
import repo.build.Pom
import repo.build.RepoEnv

class BuildPomCommand extends AbstractCommand {
    BuildPomCommand() {
        super('build-pom', 'Generate main pom.xml for all components')
    }

    void execute(RepoEnv env, CliOptions options) {
        def pomFile = options.getPomFile()
        def dirName = pomFile.parentFile.name
        if(dirName == '.') {
            dirName = pomFile.parentFile.parentFile.name
        }
        def nameSuffix = options.hasFeatureBransh() ?
                options.getFeatureBranch() :
                Git.getBranch(new File(options.getRepoBasedir(), "manifest"))
        Pom.generateXml(env, "$dirName-$nameSuffix", pomFile)
    }
}
