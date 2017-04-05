package repo.build.command

import groovy.transform.CompileStatic
import repo.build.MavenFeature
import repo.build.CliOptions
import repo.build.RepoEnv

class ReleaseUpdateVersionsCommand extends AbstractCommand {
    ReleaseUpdateVersionsCommand() {
        super('release-update-versions', 'Update depeendencies for current release')
    }

    void execute(RepoEnv env, CliOptions options) {
        def includes = options.getIncludes()
        def continueFromComponent = options.getContinueFromComponent()
        MavenFeature.updateVersions(env,
                options.getFeatureBranch(), includes, continueFromComponent, false)
    }
}
