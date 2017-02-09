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
        def buildPomFile = options.getPomFile()
        def nameSuffix = options.hasFeatureBransh() ?
                options.getFeatureBranch() :
                Git.getBranch(new File(options.getRepoBasedir(), "manifest"))
        Pom.generateXml(env, nameSuffix, buildPomFile)
    }
}
