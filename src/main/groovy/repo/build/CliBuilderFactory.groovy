package repo.build

import groovy.transform.CompileStatic;

class CliBuilderFactory {
    static CliBuilder build() {
        def cli = new CliBuilder(usage: 'repo-build -[rpfmtsMbjd] ' +
                '[init ' +
                '| sync ' +
                '| build-pom ' +
                '| switch ' +
                '| prepare-merge ' +
                '| export-bundles ' +
                '| status ' +
                '| grep ' +
                '| merge-abort ' +
                '| stash ' +
                '| stash-pop]*')
        cli.with {
            r( args:1, argName: 'repoBasedir','base dir of repo projects')
            p( args:1, argName: 'buildPomFile', '')
            f( args:1, argName: 'featureBranch', 'feature branch name')
            t( args:1, argName: 'targetExportDir', 'target export dir for bundles')
            s( args:1, argName: 'sourceImportDir', 'source  import dir for bundles')
            M( args:1, argName: 'manifestGitUrl', '')
            b( args:1, argName: 'manifestBranchName', '')
            j( args:1, argName: 'parallel', '')
            e( args:1, argName: 'expr', 'regexp for search')
            d( 'detach from branches' )
            m( 'use manifest branches' )
            a( 'execute merge --abort before merge' )
            X( 'Enable debug mode' )
        }
        return cli
    }
}
