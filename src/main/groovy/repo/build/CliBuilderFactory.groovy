package repo.build

import com.google.common.base.Joiner
import groovy.transform.CompileStatic;

class CliBuilderFactory {
    static final String CMD_INIT = 'init'
    static final String CMD_SYNC = 'sync'
    static final String CMD_BUILD_POM = 'build-pom'
    static final String CMD_SWITCH = 'switch'
    static final String CMD_PREPARE_MERGE = 'prepare-merge'
    static final String CMD_EXPORT_BUNDLES = 'export-bundles'
    static final String CMD_STATUS = 'status'
    static final String CMD_GREP = 'grep'
    static final String CMD_MERGE_ABORT = 'merge-abort'
    static final String CMD_STASH = 'stash'
    static final String CMD_STASH_POP = 'stash-pop'
    static final String CMD_FEATURE_MERGE_RELEASE = 'feature-merge-release'
    static final String CMD_FEATURE_UPDATE_PARENT = 'feature-update-parent'
    static final String CMD_FEATURE_UPDATE_VERSIONS = 'feature-update-versions'
    static final String CMD_RELEASE_MERGE_FEATURE = 'release-merge-feature'
    static final String CMD_RELEASE_UPDATE_PARENT = 'release-update-parent'
    static final String CMD_RELEASE_UPDATE_VERSIONS = 'release-update-versions'
    static final String CMD_PUSH_FEATURE = 'push-feature'
    static final String CMD_PUSH_MANIFEST = 'push-manifest'

    static CliBuilder build() {
        def cli = new CliBuilder(usage: 'repo-build -[rpfmtsMbjd] ' +
                Joiner.on('\n').join(
                        '\n[',
                        CMD_INIT,
                        CMD_SYNC,
                        CMD_BUILD_POM,
                        CMD_SWITCH,
                        CMD_PREPARE_MERGE,
                        CMD_EXPORT_BUNDLES,
                        CMD_STATUS,
                        CMD_GREP,
                        CMD_MERGE_ABORT,
                        CMD_STASH,
                        CMD_STASH_POP,
                        CMD_FEATURE_MERGE_RELEASE,
                        CMD_FEATURE_UPDATE_PARENT,
                        CMD_FEATURE_UPDATE_VERSIONS,
                        CMD_RELEASE_MERGE_FEATURE,
                        CMD_RELEASE_UPDATE_PARENT,
                        CMD_RELEASE_UPDATE_VERSIONS,
                        CMD_PUSH_FEATURE,
                        CMD_PUSH_MANIFEST,
                        ']*'
                ).toString())
        cli.with {
            a( 'execute merge --abort before merge' )
            b( args:1, argName: 'manifestBranchName', '')
            d( 'detach from branches' )
            e( args:1, argName: 'expr', 'regexp for search')
            f( args:1, argName: 'featureBranch', 'feature branch name')
            i( args:1, argName: 'includes', 'maven artifact includes list')
            j( args:1, argName: 'parallel', '')
            m( 'use manifest branches' )
            p( args:1, argName: 'buildPomFile', '')
            r( args:1, argName: 'repoBasedir','base dir of repo projects')
            s( args:1, argName: 'sourceImportDir', 'source  import dir for bundles')
            t( args:1, argName: 'targetExportDir', 'target export dir for bundles')
            M( args:1, argName: 'manifestGitUrl', '')
            P( args:1, argName: 'parent', 'parent component name')
            C( args:1, argName: 'continue', 'continue from component')
            X( 'enable debug mode' )


            posix = false
            stopAtNonOption = false
        }
        return cli
    }
}
