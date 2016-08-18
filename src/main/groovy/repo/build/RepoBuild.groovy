package repo.build

import groovy.util.CliBuilder;

class RepoBuild {
    final CliBuilder cli
    final String[] args
    OptionAccessor options
    RepoEnv env

    RepoBuild(args) {
        this.cli = CliBuilderFactory.build()
        this.args = args
    }

    static main(args) {
        def repoBuild = new RepoBuild(args)
        repoBuild.execute()
    }



    void execute() {
        options = cli.parse(args)
        env = new RepoEnv(getRepoBasedir())

        def commands = options.arguments();
        commands.each {
            switch(it) {
                case "build-pom":
                    doBuildPom()
                    break;
                case "switch":
                    doSwitch()
                    break;
                case "prepare-merge":
                    doPrepareMerge()
                    break;
                case "import-bundles":
                    doImportBundles()
                    break;
                case "exportBundles":
                    doExportBundles()
                    break;
                case "init":
                    doInit()
                    break;
                case "sync":
                    doSync()
                    break;
                default:
                    println cli.usage()
            }
        }
    }

    File getRepoBasedir() {
        return options.r ? new File(options.r) : new File(".")
    }

    void doBuildPom() {
        def buildPomFile = options.p ?
                new File(options.p) :
                new File(getRepoBasedir(),"pom.xml")
        def featureBranch = options.f ?
                options.f :
                RepoManifest.getBranch(env, "manifest")
        Pom.generateXml(env, featureBranch, buildPomFile)
    }

    void doSwitch() {
        def featureBranch = options.f
        if( featureBranch ) {
            Git.switchToBranch(env, featureBranch)
        } else {
        }
    }

    void doPrepareMerge() {
        def featureBranch = options.f
        if( !featureBranch) {
            throw new RepoBuildException("featureBranch required")
        }
        Git.mergeFeatureBranch(env, featureBranch )
    }

    void doImportBundles() {
    }

    void doExportBundles() {
        def featureBranch = options.f
        if( !featureBranch) {
            throw new RepoBuildException("featureBranch required")
        }
        def targetExportDir = options.t ?
                new File(options.t)
                : new File(getRepoBasedir(), featureBranch)

        targetExportDir.mkdirs()
        Git.createFeatureBundles(env, featureBranch, targetExportDir )
    }

    void doInit() {
    }

    static void doSync(options) {
    }
}
