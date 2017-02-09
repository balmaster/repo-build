package repo.build

import com.google.common.base.Joiner

class CliBuilderFactory {

    static CliBuilder build(CommandRegistry commandRegistry) {
        def cli = new CliBuilder(usage: 'repo-build -[rpfmtsMbjd] ' +
                Joiner.on('\n').join(
                        commandRegistry.getCommands().each {
                            "\n${it.name}\n${it.description}\n"
                        }
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
