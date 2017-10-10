package repo.build

class CliBuilderFactory {

    static CliBuilder build(String usage) {
        def cli = new CliBuilder()
        cli.setUsage(usage)
        cli.with {
            a('execute merge --abort before merge')
            b(args: 1, argName: 'manifestBranchName', '')
            d('detach from branches')
            e(args: 1, argName: 'expr', 'regexp for search')
            f(args: 1, argName: 'featureBranch', 'feature branch name')
            fae('Only fail the foreach afterwards;allow all iteration to continue')
            i(args: 1, argName: 'includes', 'maven artifact includes list')
            j(args: 1, argName: 'parallel', '')
            m('use manifest branches')
            p(args: 1, argName: 'buildPomFile', '')
            r(args: 1, argName: 'repoBasedir', 'base dir of repo projects')
            s(args: 1, argName: 'sourceImportDir', 'source  import dir for bundles')
            t(args: 1, argName: 'targetExportDir', 'target export dir for bundles')
            all('show all output on status')
            D(args: 2, argName: 'property=value', 'system property', valueSeparator: '=', optionalArg: true)
            I(args: 1, argName: 'taskBranch', 'task branch', optionalArg: true)
            M(args: 1, argName: 'manifestGitUrl', '')
            P(args: 1, argName: 'parent', 'parent component name')
            C(args: 1, argName: 'continue', 'continue from component')
            T(args: 1, argName: 'tag', 'tag')
            W('pause before exit')
            X('enable debug mode')
            _(longOpt: 'version', 'Show version')

            // maven keys
            me('Produce execution error messages')
            mfae('Only fail the execute afterwards;allow all non-impacted builds to continue')
            mgs(args: 1, 'Alternate path for the global settings file')
            mlr(args: 1, 'Maven local repository')
            mo('Work offline')
            mP(args: 1, 'Comma-delimited list of profiles to activate')
            ms(args: 1, 'Alternate path for the user settings file')
            mT(args: 1, 'Thread count, for instance 2.0C where C is core multiplied')
            mU('Forces a check for missing releases and updated snapshots on remote repositories')

            posix = false
            stopAtNonOption = false
        }
        return cli
    }
}
