package repo.build.command

import groovy.transform.CompileStatic
import repo.build.MavenFeature
import repo.build.CliOptions
import repo.build.RepoEnv

class ReleaseUpdateParentCommand extends AbstractCommand {
    ReleaseUpdateParentCommand() {
        super('release-update-parent', 'Update parent for each component')
    }

    void execute(RepoEnv env, CliOptions options) {
        def parentComponent = options.getParent()
        MavenFeature.updateParent(env, options.getFeatureBranch(), parentComponent, true, false)
    }
}
