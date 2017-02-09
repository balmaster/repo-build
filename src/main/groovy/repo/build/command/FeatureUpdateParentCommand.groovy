package repo.build.command

import groovy.transform.CompileStatic
import repo.build.MavenFeature
import repo.build.CliOptions
import repo.build.RepoEnv

@CompileStatic
class FeatureUpdateParentCommand extends AbstractCommand {
    FeatureUpdateParentCommand() {
        super('feature-update-parent', 'Update parent for each componnent with feature branch')
    }

    void execute(RepoEnv env, CliOptions options) {
        def parentComponent = options.getParent()
        MavenFeature.updateParent(env, options.getFeatureBranch(), parentComponent, false, true)
    }
}
