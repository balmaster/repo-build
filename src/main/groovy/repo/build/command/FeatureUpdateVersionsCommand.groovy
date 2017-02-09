package repo.build.command

import groovy.transform.CompileStatic
import repo.build.MavenFeature
import repo.build.CliOptions
import repo.build.RepoEnv

@CompileStatic
class FeatureUpdateVersionsCommand extends AbstractCommand {
    FeatureUpdateVersionsCommand() {
        super('feature-update-versions', 'Update dependencies for each componet with feature branch')
    }

    void execute(RepoEnv env, CliOptions options) {
        def includes = options.getIncludes()
        def continueFromComponent = options.getContinueFromComponent()
        MavenFeature.updateVersions(env, options.getFeatureBranch(), includes, continueFromComponent, true)
    }
}
